<img src="https://raw.githubusercontent.com/undeadjess/serverjars/refs/heads/main/web/public/assets/images/favicon.png" width="100">

# ServerJars Auto-Updater

A drop-in replacement for your Minecraft server jar. Automatically downloads the latest version of your selected server type when started. Powered by ServerJars, which builds and hosts the latest Minecraft server versions in one place.

## Usage

> ServerJars and this auto-updater are not yet ready for production use

1. Download the latest version of the jar from Releases
2. Place the jar in your server directory, replacing your current server jar
3. Start your server as you normally would, and follow the prompts to select your server type and version

## Future Plans
 - [ ] it doesnt need to downlaod the file if it already exists
 - [ ] it should check if the file is up to date
    - [ ] it should allow the user to specify the version or build as "latest" so it actually has something to update
    - [ ] it should save the current version and build in the config so it can check if the file is up to date
 - [ ] can i actually store the json.jar thing in the repo or will someone get mad at me? probably better to have the build script download it anyway
