# Secret-IDE-Plugin
[![Downloads](https://img.shields.io/jetbrains/plugin/d/18939)](https://plugins.jetbrains.com/plugin/18939-secret-ide) ![Rating](https://img.shields.io/jetbrains/plugin/r/stars/18939)

<!-- Plugin description -->
This plugin enables a developer to easily build, deploy and instantiate contracts on the Secret Network, and provides an integrated GUI to view the contracts you deployed and their instances.
<!-- Plugin description end -->

## Getting Started

### Running the IDE

You can either run the pluging on your local version of IntelliJ IDEA by installing the Secret IDE from the plugins marketplace. Search for the Secret IDE in the plugins marketplace.

Or you can pull the docker image and run the docker container. The docker container allows you to connect to the IDE in your web browser by visiting https://localhost:8888. To pull the image run:

`docker pull ghcr.io/digiline-io/secret-ide:latest`

To run the container, run:

`docker run -p 8888:8888 -v $(PWD)/data:/home/secret-ide-user/ -it ghcr.io/digiline-io/secret-ide:latest`

### Creating Your First Contract 

You can quickly start from a contract template. A library of contract templates is located in the project folder named, 'Secret Network Contract' The simplest contract template is 'Secret Contract Starter'

After opening Secret Contract Starter you should see this:

![secret-contract-starter](/documentation-imgs/secret-contract-starter.png)

Now you're ready to modify this starter however you see fit. If you are new to Secret contracts then we've got a few steps you can follow below to deploy your first contract.

1. In contracts.rs:

replace the init function with 

```rust
pub fn init<S: Storage, A: Api, Q: Querier>(
    deps: &mut Extern<S, A, Q>,
    env: Env,
    msg: InitMsg,
) -> StdResult<InitResponse> {
    let config = Config {
        owner: deps.api.canonical_address(&env.message.sender)?,
    };

    let prng_seed: Vec<u8> = sha_256(base64::encode(msg.entropy).as_bytes()).to_vec();
    save(&mut deps.storage, CONFIG_KEY, &config)?;
    save(&mut deps.storage, PRNG_SEED_KEY, &prng_seed)?;

    Ok(InitResponse::default())
}
```

replace the handle function with
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

replace the handle_ex function with
```rust
/// Returns HandleResult
///
/// Handle registering a contract
///
/// # Arguments
///
/// * `deps` - mutable reference to Extern containing all the contract's external dependencies
/// * `env` - Env of contract's environment
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

replace the query function with
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
replace the query_ex function with
```rust 
fn query_secret_message<S: Storage, A: Api, Q: Querier>(deps: &Extern<S, A, Q>, viewing_key: String, user: HumanAddr) -> StdResult<QueryAnswer> {
    let addr = deps.api.canonical_address(&user)?;
    let read_key = ReadonlyPrefixedStorage::new(PREFIX_VIEW_KEY, &deps.storage);
    check_key(&deps.storage, &addr, viewing_key)?;
    let msg = may_load(&read_key, addr.as_slice())?.unwrap_or_else(|| "".to_string());

    Ok(QueryAnswer::SecretMessage {
        message: msg
    })
}
```

2. In msg.rs:



### Deploying to Testnet

### Deploying to Mainnet

### Instantiating a Contract

## Creating a common SNIP721 Contract from a Form based GUI

At the project start screen, instead of clicking 'Secret Network Contract', open the project named, 'Secret Network SNIP721 Contract'. Now you will see a form that provides all options for creating SNIP721 contract, also knowns commonly as NFTs.

## Reporting Issues

## Intellij IDEA vs. VSCode

## Known IntelliJ IDEA Issues
* when running from the web browser, you likely will need to resize your window before the IDE will go full screen

* remove the startup terms and conditions windows