# Secret-IDE-Plugin
[![Downloads](https://img.shields.io/jetbrains/plugin/d/18939)](https://plugins.jetbrains.com/plugin/18939-secret-ide) ![Rating](https://img.shields.io/jetbrains/plugin/r/stars/18939)

<!-- Plugin description -->
This plugin enables a developer to easily build, deploy and instantiate contracts on the Secret Network, and provides an integrated GUI to view the contracts you deployed and their instances.
<!-- Plugin description end -->

## Getting Started

### Running the IDE

#### Using a Docker container (**recommended**)

You can pull the docker image and run the docker container. The docker container allows you to connect to the IDE in your web browser by visiting https://localhost:8888. To pull the image run:

`docker pull ghcr.io/digiline-io/secret-ide:latest`

To run the container, run:

`docker run -p 8888:8888 -v $(PWD)/data:/home/secret-ide-user/ -it ghcr.io/digiline-io/secret-ide:latest`

#### By installing the Secret IDE plugin

You can run the plugin on your local version of IntelliJ IDEA by installing the Secret IDE from the plugins marketplace. Search for the Secret IDE in the plugins marketplace.

### Creating Your First Contract

You can quickly start from a contract template. A library of contract templates is located in the project folder named, 'Secret Network Contract' The simplest contract template is 'Secret Contract Starter'

After opening Secret Contract Starter you should see this:

![secret-contract-starter](/documentation-imgs/secret-contract-starter.png)

Now you're ready to modify this starter however you see fit. If you are new to Secret contracts then we've got a few steps you can follow below to deploy your first, "hello user" contract.

First you're going to replace the default testing handle with a handle function that works with a Register message. In contract.rs you look for the handle function and look for the match statement, it should look something like this:

```rust
pub fn handle<S: Storage, A: Api, Q: Querier>(
    deps: &mut Extern<S, A, Q>,
    env: Env,
    msg: HandleMsg,
) -> StdResult<HandleResponse> {
    match msg {
        HandleMsg::HandleEx {} => handle_ex(deps, env), // <------- this is what you need to look for
    }
}
```

You can then replace HandleEx with Register and add a name argument, then change the function called to something like handle_register. If you did this correctly the function should look something like this:

```rust
pub fn handle<S: Storage, A: Api, Q: Querier>(
    deps: &mut Extern<S, A, Q>,
    env: Env,
    msg: HandleMsg,
) -> StdResult<HandleResponse> {
    match msg {
        HandleMsg::Register { name } => handle_register(deps, env, name),
    }
}
```

And now it's time to actually create the handle_register function, if you look at the code in contract.rs you should see a function called handle_ex. Rename that function to handle_register and add a name argument, then change its code to store the name argument under the key with the user's address in storage and create a viewing key for that information afterwards. If you're unsure about how to do that, the code should look something like this:

```rust
/// Returns HandleResult
///
/// Handle registering a user
///
/// # Arguments
///
/// * `deps` - mutable reference to Extern containing all the contract's external dependencies
/// * `env` - Env of contract's environment
/// * `name` - the name of the user
pub fn handle_register<S: Storage, A: Api, Q: Querier>(
    deps: &mut Extern<S, A, Q>,
    env: Env,
    name: String,
) -> HandleResult {
    let mut msg_store = PrefixedStorage::new(PREFIX_NAMES, &mut deps.storage);
    let message_sender = deps.api.canonical_address(&env.message.sender)?;
    save(&mut msg_store, message_sender.as_slice(), &name)?;
    return create_key(deps, env, &name.as_str());
}
```

Now it's time to create the query; look for the function called query and in the match expression replace QueryEx with something like Secret Message that takes two parameters, the viewing key and the user's address. The function should now look like this:

```rust
pub fn query<S: Storage, A: Api, Q: Querier>(
    deps: &Extern<S, A, Q>,
    msg: QueryMsg,
) -> StdResult<Binary> {
    match msg {
        QueryMsg::SecretMessage { viewing_key, user } => to_binary(&query_secret_message(deps, viewing_key, contract)?),
    }
}
```

Now let's make your query actually return something, in this small tutorial the goal is to just send the user their message back if the correct viewing key is sent, so you can replace the  the query_ex function with the following:
```rust
fn query_secret_message<S: Storage, A: Api, Q: Querier>(deps: &Extern<S, A, Q>, viewing_key: String, user: HumanAddr) -> StdResult<QueryAnswer> {
    let addr = deps.api.canonical_address(&user)?;
    let read_key = ReadonlyPrefixedStorage::new(PREFIX_VIEW_KEY, &deps.storage);
    check_key(&deps.storage, &addr, viewing_key)?;
    let names_key = ReadonlyPrefixedStorage::new(PREFIX_NAMES, &deps.storage);
    let msg = may_load(&names_key, addr.as_slice())?.unwrap_or_else(|| "".to_string());

    Ok(QueryAnswer::SecretMessage {
        message: msg
    })
}
```

This function reads the key for the specified user, checks whether it's the same as the key specified and if it is, it reads the message and sends it to the user.

Now you're done with your contract's code, but there are a few things missing, all of the messages you used above and all the response types need to be defined, so to do that you should edit the msg.rs file.

To do this, replace everything after InitMsg with the following:
```rust
#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
#[serde(rename_all = "snake_case")]
pub enum HandleMsg {
    Register {
        name: String,
    },
}

#[derive(Serialize, Deserialize, JsonSchema, Debug)]
#[serde(rename_all = "snake_case")]
pub enum HandleAnswer {
    ViewingKey {
        key: String,
    },
}



#[derive(Serialize, Deserialize, Clone, Debug, PartialEq, JsonSchema)]
#[serde(rename_all = "snake_case")]
pub enum QueryMsg {
    SecretMessage {
        user: HumanAddr,
        viewing_key: String,
    }
}

#[derive(Serialize, Deserialize, JsonSchema, Debug)]
#[serde(rename_all = "snake_case")]
pub enum QueryAnswer {
    SecretMessage {
        message: String,
    }
}
```

There's one final thing left, you may have noticed that we use the storage key PREFIX_NAMES, you will need to define that in state.rs, to do so just add
```rust
pub static PREFIX_NAMES: &[u8] = b"names";
```

after the following line:
```rust
pub const PREFIX_VIEW_KEY: &[u8] = b"viewkeys";
```

if anything needs to be imported do so by pressing alt + shift + enter or option (⌥) + shift + enter, do this in both contract.rs and msg.rs.

### Building your contract
If you look at the top right portion of your IDE you will see a dropdown with a few options, as well as a green "start button", select the option that says build and click the start button to compile your contract.

![compile-your-contract](/documentation-imgs/compile.png)

### Deploying
On the bottom right side of your screen you should see a panel with the name "Deploy and Instantiate". If you click that you should be able to see a form where you input your wallet seed (please don't use your main wallet while Secret IDE is in beta) and you should see a select box with two options, pulsar-2 testnet and secret-4 mainnet. Select the network you want (we recommend testnet while developing your contract and mainnet once you're done), then  press "Deploy". In the future we're planning to add an option to deploy to localsecret as well.

![deploy-and-instantiate](/documentation-imgs/deploy_and_instantiate.png)

### Instantiating a Contract
Once the contract is deployed, you should see the code id in the terminal, copy that code ID and input it in the code ID field in the same window, then give your contract a label (this needs to be unique on the secret network, so maybe append the date and time at the end) and an input message. The input message is a JSON formatted message with every argument needed to instantiate your contract.

## Creating a common SNIP721 Contract from a Form based GUI

At the project start screen, instead of clicking 'Secret Network Contract', open the project named, 'Secret Network SNIP721 Contract'. Now you will see a form that provides all options for creating SNIP721 contract, also known commonly as NFTs.

## Reporting Issues
Please report any issues on [our github](https://github.com/digiline-io/Secret-IDE-Plugin) using the issue templates we defined.

## Intellij IDEA vs. VSCode
This question is asked very often; IntelliJ IDEA is an IDE while VSCode is a good text editor. IntelliJ IDEA provides a lot of features that make development easier once you're used to it, this includes but is not limited to:

- Better Rust support out-of-the-box with the rust plugin
- Better Intellisense, the IDE analyzes the code in real time, it understands the code, making development much easier
- It's "batteries included", every little thing you may need is usually included by default or is just a plugin away

## Known Secret IDE/IntelliJ IDEA Issues
There are some known issues when running the IDE in the web browser:
* You likely will need to resize your window before the IDE will go full screen, the IDE starts with a small default resolution when running in the browser.
* Sometimes some modals get stuck in an in-between-state where they appear and disappear, this can be solved by just pressing escape a few times

You can also run the Secret IDE plugin to avoid these browser issues