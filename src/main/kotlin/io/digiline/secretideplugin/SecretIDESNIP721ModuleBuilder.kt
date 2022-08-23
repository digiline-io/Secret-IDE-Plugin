package io.digiline.secretideplugin

import cloneRepo
import com.intellij.execution.RunManager
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.roots.ModifiableRootModel
import createProjectFromSubFolderInRepo
import createProjectUsingCargoGenerate
import java.nio.file.Paths
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.Disposer
import com.intellij.util.io.readText
import com.intellij.util.io.write
import createBuildActionsFromMakefile
import java.util.UUID.randomUUID
import kotlin.math.roundToInt


class SecretIDESNIP721ModuleBuilder : ModuleBuilder() {
  var data: SecretNetworkSNIP721ContractProjectCreationWizard? = null;

  override fun setupRootModel(model: ModifiableRootModel) {
    val root = doAddContentEntry(model)?.file ?: return
    model.inheritSdk()
    root.refresh(false, true)
    val project = model.project
    val name = project.name.replace(' ', '_')
    object : Task.Modal(project, "Creating Project", false) {
      override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = true
        val template = ContractTemplate(
          RepoType.url,
          "https://github.com/luminaryphi/secret-random-minting-snip721-impl.git",
          "secret-random-minting-snip721-impl",
          "",
        )
        val path = Paths.get(project.basePath)
        cloneRepo(template.url, path, name)
        val deployFile = path.resolve("deploy.sh")
        val initJsonFile = path.resolve("init.json")
        val contractRsFile = path.resolve("src/contract.rs")
        val entropy = randomUUID().toString()
        val royalty = ((data!!.royaltyPercentage.text).toFloat() * 100).roundToInt()
        deployFile.write(
          """
          #!/bin/bash
          set -e
          cd ${path.toAbsolutePath()}
          make compile-optimized
          network=""
          while [[ "testnet" != "${'$'}network" && "mainnet" != "${'$'}network" ]]; do
            read -p "Enter Network Name [testnet/mainnet]: " network
          done
          if [[ "testnet" == "${'$'}network" ]]; then
            echo "Deploying to testnet"
            secretcli config chain-id pulsar-2
            secretcli config node https://rpc.pulsar.scrttestnet.com
          else
            echo "Deploying to mainnet"
            secretcli config chain-id secret-4
            secretcli config node https://scrt-validator.digiline.io:26657
          fi
          secretcli config keyring-backend test
          secretcli config broadcast-mode block
          read -r -s -p "Enter wallet seed for deployment (seed will be hidden): " seed
          secretcli keys delete SecretIDE-Deployment -y
          echo "${'$'}seed" | secretcli keys add SecretIDE-Deployment --recover
          clear
          codeId=${'$'}(secretcli tx compute store contract.wasm.gz --from SecretIDE-Deployment --gas "${'$'}gas" -y | jq '.logs[0].events[0].attributes[3].value')
          echo "Contract stored successfully! Code ID: ${'$'}codeId"
          initMsg=${'$'}(cat init.json)
          secretcli tx compute instantiate "${'$'}codeId" --from SecretIDE-Deployment --gas "${'$'}gas" "${'$'}initMsg"
          """.trimIndent()
        )
        initJsonFile.write(
          """
            {
              "name": "${data!!.collectionName.text}",
              "symbol": "${data!!.collectionSymbol.text}",
              "entropy": "$entropy",
              "royalty_info": {
                "decimal_places_in_rates": 4,
                "royalties": [
                  {
                    "recipient": "${data!!.royaltyAddress.text}",
                    "rate": $royalty
                  }
                ]
              },
              "config": {
                "public_token_supply": ${!data!!.tokenSupplyPrivate.isSelected},
                "public_owner": ${data!!.ownerPublic.isSelected},
                "enable_sealed_metadata": ${data!!.metadataSealed.isSelected},
                "unwrapped_metadata_is_private": ${!data!!.unwrappedMetadataPublic.isSelected},
                "minter_may_update_metadata": ${data!!.minterCanUpdateMetadata.isSelected},
                "owner_may_update_metadata": ${data!!.ownerCanUpdateMetadata.isSelected},
                "enable_burn": ${data!!.burnable.isSelected},
              },
              "snip20_hash": "AF74387E276BE8874F07BEC3A87023EE49B0E7EBE08178C49D0A49C3C98ED60E",
              "snip20_address": "secret1k0jntykt7e4g3y88ltc60czgjuqdy4c9e8fzek",
              "mint_funds_distribution_info": {
                "decimal_places_in_rates": 4,
                "royalties": [
                  {
                    "recipient": "${data!!.royaltyAddress.text}",
                    "rate": 10000
                  }
                ]
              }
            }
          """.trimIndent()
        )
        contractRsFile.write(
          contractRsFile
            .readText()
            .replace(
              "pub const MINT_COST: u128 = 10000000",
              "pub const MINT_COST: u128 = ${data!!.price.text.toLong() * 1_000_000}"
            )
        )
        createBuildActionsFromMakefile(project, path)
        val runManager = RunManager.getInstance(project)
        val config = runManager.createConfiguration(
          "quick-deploy",
          SecretNetworkContractConfigurationFactory(
            SecretNetworkContractConfigurationType()
          ),
        )
        runManager.addConfiguration(config)
        runManager.selectedConfiguration = config
        var makefileContents = path.resolve("Makefile").readText()
        makefileContents += "\n\nquick-deploy:\n\tbash ./deploy.sh"
        path.resolve("Makefile").write(makefileContents)
      }
    }.queue()
  }

  @Suppress("DialogTitleCapitalization")
  override fun getModuleType(): SecretIDESNIP721ModuleType {
    return SecretIDESNIP721ModuleType.instance
  }

  override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep {
    return SecretIDESNIP721ModuleWizardStep(this, context).apply {
      Disposer.register(parentDisposable, this::disposeUIResources)
    }
  }

  @Throws(ConfigurationException::class)
  override fun validateModuleName(moduleName: String): Boolean {
    val errorMessage = RustPackageNameValidator.validate(moduleName, true) ?: return true
    throw ConfigurationException(errorMessage)
  }
}
