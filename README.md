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

    Please see [WIKI PAGE TO BE CREATED] if you have the game installed in a different location, or you receive a `BUILD FAILED` with one of the following errors when building the project:
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

5. Build the project:
    To build the project, run:
    ```
    ./gradlew build
    ```
    ***
