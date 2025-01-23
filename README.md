## Building Wilderforge

### Prerequisites

If order to build WilderForge, you must have the following software installed:

* Git
* Java 17 or later
* Wildermyth

__Note:__ This project uses WilderWorkspace to configure itself for IDEs. Currently, WilderWorkspace only supports the Eclipse IDE. Pull Requests to add support for additional IDEs are welcome, and can be submitted at the [WilderWorkspace repository](https://wildermods.com/wilderworkspace)

### Build Instructions

1. Clone the git repository by executing the following command:

    ```shell
    git clone git@github.com:WilderForge/WilderForge.git
    ```
    ***
2. Navigate into the project's main directory:

    ```shell
    cd ./wilderforge
    ```
    ***
3. Setup the project workspace:

    ```shell
    ./gradlew setupDecompWorkspace
    ```

    __Important:__ This process will take a while. You will see errors appear in the console. Usually these are related to issues with decompiling koltin files. If you see `BUILD SUCCESSFUL` after the command has executed, you may proceed to the next step.

    __Important:__ by default, WilderWorkspace assumes the base game files are located in the default directory for a steam installation on your operating system. 

    Please see [WIKI PAGE TO BE CREATED] if you have the game installed in a different location, or you receive a `BUILD FAILED` with one of the following errors when setting up the workspace:
    ```
      java.io.FileNotFoundException: [STEAM_DIRECTORY]/steamapps/common/Wildermyth
    ```
    ```
      org.apache.commons.lang3.NotImplementedException: I don't know where the default install directory for Wildermyth is for the [PLATFORM] platform. Submit a pull request or input a raw path to the installation location.
    ```

    ***
4. Configure the project for your IDE:

    Execute the following commands to configure the project for your IDE. If you only want to build the jar, and don't want to modify WilderForge itself, you can skip this step.

    __Note:__ This project uses WilderWorkspace to configure itself for IDEs. Currently, WilderWorkspace only supports the Eclipse IDE. Pull Requests to add support for additional IDEs are welcome, and can be submitted at the [WilderWorkspace repository](https://wildermods.com/wilderworkspace)

    Configure the project
    ```shell
    ./gradlew eclipse --refresh-dependencies
    ```

    Generate run configurations to allow your IDE to start and debug the game:
    ```shell
    ./gradlew genEclipseRuns
    ```
    ***

    You should now be able to import the project into your IDE and make changes to WilderForge.

5. Build the project:

    To build the project, run:
    ```shell
    ./gradlew build
    ```
    ***

    This will create the mod jar file in `[project directory]/build/libs/`.

## Updating WilderWorkspace

The workspace plugin may occasionally receive updates. To update WilderWorkspace, you must do the following:

1. Open the following file: `[project root]/gradle/libs.versions.toml`
   ***
2. Change the value of `workspace_version` to the desired version.
    ***
3. Notify your IDE of the changes:
    ```
    ./gradlew eclipse --refresh-dependencies
    ```
    ***
## Updating Wildermyth

__Extremely Important:__ Updating the base game requires deletion of the copy of the game in the workspace, including save data and legacy files. The original game (from Steam, GOG, etc.) will remain unaffected.

If the base game receives an update and you wish to build against it, follow these steps:

1. Ensure the base game is up to date on your system.
    ***
2. Execute the following command to delete the old version from the workspace:

    ```shell
    ./gradlew clearLocalRuntime
    ```
    ***
3. Setup the project workspace again
    ```
    ./gradlew setupDecompWorkspace
    ```
    ***
4. Notify your IDE of the changes:
    ```
    ./gradlew eclipse --refresh-dependencies
    ```
    ***
