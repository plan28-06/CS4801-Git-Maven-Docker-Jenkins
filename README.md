# End-to-End CI/CD Pipeline: Java 25 + Maven + Git + Docker + Jenkins

This guide assumes the following setup:

-   GitHub repository: `gitmavenpractice`
-   Git branch: `main`
-   Windows 11
-   Java 25 installed
-   Apache Maven 3.10.0 installed
-   Jenkins installed directly on Windows and running on port `8080`
-   Jenkins URL: `http://localhost:8080`
-   Docker Desktop installed and running
-   Jenkins job name: `gitmavenpractice-pipeline`
-   Java application listens on port `8080` inside its container
-   Host port `8081` maps to container port `8080`

Replace `YOUR_USERNAME` with your GitHub username. Use your actual local
project path when running commands.

### Port summary

  Component                            Port
  ---------------------------------- ------
  Jenkins on Windows                   8080
  Java application inside Docker       8080
  Java application on Windows host     8081
  ngrok forwarding target              8080

Jenkins and the Java application can both use port 8080 because the
application listens on port 8080 inside the container, while Docker
publishes it on host port 8081.



## 6. Create `pom.xml`

Create `pom.xml` in the project root and paste:

``` xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>gitmavenpractice</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.release>25</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
        </plugins>
    </build>
</project>
```

### What does `pom.xml` do?

-   `groupId`, `artifactId`, and `version` identify the Maven project.
-   `maven.compiler.release` tells Maven to compile for Java 25.
-   JUnit Jupiter provides the unit-testing framework.
-   Maven Compiler Plugin compiles the Java source.
-   Maven Surefire Plugin executes the unit tests.

Validate the configuration:

``` powershell
mvn validate
```

Expected result: `BUILD SUCCESS`.

If Maven reports that release 25 is unsupported, verify the Java version
Maven actually uses with `mvn -version`. If Maven is using Java 25 but
the compiler plugin still reports a compatibility problem, update the
Maven Compiler Plugin to a version that supports Java 25.

## 7. Create `App.java`

Create this file:

`src/main/java/com/example/studentmanagement/App.java`

Paste and save:

``` java
package com.example.studentmanagement;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class App {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0);

        server.createContext("/status", exchange -> {
            byte[] response = "Application is working".getBytes();
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        server.start();
        System.out.println("Server running on port 8080");
    }

    public static String getStatus() {
        return "Application is working";
    }
}
```

This small application starts an HTTP server and exposes `/status`. The
endpoint returns a text response. The `getStatus()` method is used by
the unit test.

## 8. Create `AppTest.java`

Create:

`src/test/java/com/example/studentmanagement/AppTest.java`

Paste and save:

``` java
package com.example.studentmanagement;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {
    @Test
    void testGetStatus() {
        assertEquals("Application is working", App.getStatus());
    }
}
```

This test checks that `getStatus()` returns the expected string.

## 9. Build and test with Maven

From the project root, run:

``` powershell
mvn clean package
```

Maven will:

1.  `clean` --- remove previous build output.
2.  `compile` --- compile the Java source.
3.  `test` --- run the JUnit test.
4.  `package` --- generate the JAR.

Look for output similar to:

``` text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The generated JAR should be at:

``` text
target/gitmavenpractice-1.0-SNAPSHOT.jar
```

Do not proceed until this command succeeds.

------------------------------------------------------------------------

# Part C --- Docker image and container

## 10. Create the `Dockerfile`

Create a file named exactly `Dockerfile` in the project root. Do not add
a `.txt` extension.

``` dockerfile
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY target/gitmavenpractice-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-cp", "app.jar", "com.example.studentmanagement.App"]
```

### What does this Dockerfile do?

-   `FROM` selects a Java 25 runtime image.
-   `WORKDIR` sets the working directory inside the container image.
-   `COPY` copies the JAR produced by Maven into the image.
-   `EXPOSE 8080` documents the application's listening port.
-   `CMD` starts the Java application when the container runs.

The Docker image is built from the JAR, so Maven must run successfully
before `docker build`.

## 11. Create `.gitignore`

Create `.gitignore` in the project root:

``` gitignore
target/
.idea/
.vscode/
*.iml
```

This excludes generated Maven output and local IDE settings from Git.
Maven recreates `target/` when needed.

## 12. Build the Docker image

Make sure Docker Desktop is running. From the project root, execute:

``` powershell
docker build -t gitmavenpractice:1.0 .
```

Explanation:

-   `docker build` builds an image.
-   `-t gitmavenpractice:1.0` names and tags the image.
-   `.` uses the current directory as the build context.

List the image:

``` powershell
docker images
```

Look for image `gitmavenpractice` with tag `1.0`.

## 13. Run the container

Run:

``` powershell
docker run -d --name student-app -p 8081:8080 gitmavenpractice:1.0
```

Explanation:

-   `-d` runs the container in the background.
-   `--name student-app` gives the container a name.
-   `-p 8081:8080` maps Windows host port 8081 to container port 8080.
-   `gitmavenpractice:1.0` is the image to run.

Verify the running container:

``` powershell
docker ps
```

Open this URL in a browser:

``` text
http://localhost:8081/status
```

Expected response:

``` text
Application is working
```

### If the container name is already in use

Remove the old container:

``` powershell
docker rm -f student-app
```

Then rerun the `docker run` command.

### If host port 8081 is already in use

Use a different host port, for example:

``` powershell
docker run -d --name student-app -p 8083:8080 gitmavenpractice:1.0
```

Then test `http://localhost:8083/status`. If you change the host port,
also change the Jenkinsfile deployment command later.

------------------------------------------------------------------------

# Part D --- Git and GitHub

## 14. Initialize Git and commit the project

Ensure that your terminal is in the project root. Run:

``` powershell
git init
git add .
git commit -m "Add Java Maven application and Docker setup"
git branch -M main
```

If Git asks you to configure your identity, run:

``` powershell
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

Replace those values with your own name and email, then retry the
commit.

## 15. Connect to GitHub

The remote repository is called `gitmavenpractice`.

Check whether a remote is already configured:

``` powershell
git remote -v
```

If the correct GitHub URL is already listed, keep it. Otherwise, add the
remote:

``` powershell
git remote add origin https://github.com/YOUR_USERNAME/gitmavenpractice.git
```

Replace `YOUR_USERNAME` with your GitHub username.

Push the project:

``` powershell
git push -u origin main
```

If GitHub rejects the push because the remote already contains a README
or other commits, do not force-push. Review the error and reconcile the
histories first.

## 16. Verify GitHub

Open the repository on GitHub. The root should contain:

``` text
.gitignore
Dockerfile
Jenkinsfile     (created in Part E)
pom.xml
src/
```

When you change files later, commit and push them:

``` powershell
git add .
git commit -m "Describe the change"
git push origin main
```

------------------------------------------------------------------------

# Part E --- Create the Jenkins pipeline

## 17. Create `Jenkinsfile`

Create a file named exactly `Jenkinsfile` in the project root.

Paste this Windows-oriented declarative pipeline:

``` groovy
pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn clean package'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat 'docker build -t gitmavenpractice:1.0 .'
            }
        }

        stage('Deploy') {
            steps {
                bat 'docker rm -f student-app || exit /b 0'
                bat 'docker run -d --name student-app -p 8081:8080 gitmavenpractice:1.0'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed. Check the stage logs.'
        }
    }
}
```

### How the pipeline works

1.  **Checkout** --- checks out the repository configured in the Jenkins
    job.
2.  **Build and Test** --- runs Maven compilation, tests, and packaging.
3.  **Build Docker Image** --- builds an image from the JAR produced by
    Maven.
4.  **Deploy** --- removes the old container if present and starts a
    replacement.

The cleanup command tolerates a missing container. The `docker run`
command will still fail if Docker is unavailable or if the host port
cannot be bound.

### Important Windows/Jenkins detail

The `bat` step is for a Windows Jenkins agent. The Windows account
running Jenkins must be able to access Git, Maven, and Docker. A command
working in your personal PowerShell session does not guarantee that it
will work under the Jenkins service account.

If Jenkins reports that `mvn` or `docker` is not recognized, inspect the
service account and its `PATH`.

## 18. Commit and push the Jenkinsfile

Run:

``` powershell
git add Jenkinsfile
git commit -m "Add Jenkins CI/CD pipeline"
git push origin main
```

If the Jenkinsfile was already committed, Git may report that there is
nothing to commit.

------------------------------------------------------------------------

# Part F --- Configure Jenkins on port 8080

## 19. Open Jenkins

Open:

``` text
http://localhost:8080
```

Sign in.

## 20. Create the Pipeline job

1.  Click **New Item**.

2.  Enter the job name:

    ``` text
    gitmavenpractice-pipeline
    ```

3.  Select **Pipeline**.

4.  Click **OK**.

## 21. Configure the job to fetch from GitHub

Scroll to the **Pipeline** section.

Set:

-   **Definition:** `Pipeline script from SCM`

-   **SCM:** `Git`

-   **Repository URL:**

    ``` text
    https://github.com/YOUR_USERNAME/gitmavenpractice.git
    ```

-   **Branches to build:**

    ``` text
    */main
    ```

-   **Script Path:**

    ``` text
    Jenkinsfile
    ```

For a public repository, credentials are usually not needed for
read-only checkout. For a private repository, configure appropriate Git
credentials in Jenkins.

Click **Save**.

## 22. Run the first Jenkins build

1.  Open `gitmavenpractice-pipeline`.
2.  Click **Build Now**.
3.  Select the build number under **Build History**.
4.  Open **Console Output**.
5.  Inspect the stage view.

Expected stages:

-   Checkout
-   Build and Test
-   Build Docker Image
-   Deploy

The final status should be `Finished: SUCCESS`.

Verify the application at:

``` text
http://localhost:8081/status
```

Jenkins runs on Windows host port 8080, while the Java application is
published on host port 8081 and listens on port 8080 inside the
container.

------------------------------------------------------------------------

# Part G --- Automatically trigger builds with Poll SCM

## 23. What Poll SCM does

Poll SCM makes Jenkins check the configured Git repository periodically.
When Jenkins detects a relevant new commit, it schedules a build. If
there are no new changes, it normally does not start a build.

## 24. Configure Poll SCM

1.  Open Jenkins at `http://localhost:8080`.

2.  Open `gitmavenpractice-pipeline`.

3.  Click **Configure**.

4.  Find **Build Triggers**.

5.  Check **Poll SCM**.

6.  In the schedule field, enter:

    ``` text
    * * * * *
    ```

7.  Click **Save**.

This checks once per minute. Standard Jenkins cron-style Poll SCM
schedules do not support a 30-second interval.

The five fields are:

``` text
MINUTE HOUR DAY-OF-MONTH MONTH DAY-OF-WEEK
```

`* * * * *` means every minute.

## 25. Test Poll SCM

Make a small change to a tracked file, such as the response string in
`App.java`. Save the file, then run:

``` powershell
git add .
git commit -m "Test Poll SCM trigger"
git push origin main
```

Wait for the next poll and inspect Jenkins **Build History**. Allow a
couple of minutes for the poll and build to occur.

Expected result: a new build starts without clicking **Build Now**.

### Poll SCM troubleshooting

-   Confirm Poll SCM is checked and saved.
-   Confirm the repository URL and branch are correct.
-   Confirm the commit was pushed to GitHub.
-   Check Jenkins logs for Git or credential errors.
-   Ensure the job uses **Pipeline script from SCM**.

------------------------------------------------------------------------

# Part H --- Automatically trigger builds with a GitHub webhook

## 26. What a webhook does

A webhook is event-driven. After a push, GitHub sends a request to
Jenkins. If Jenkins receives and accepts the event, it checks the
repository and can schedule a build.

Unlike Poll SCM, a webhook does not require Jenkins to wait for the next
polling interval.

## 27. Enable the webhook trigger in Jenkins

1.  Open Jenkins at `http://localhost:8080`.

2.  Open `gitmavenpractice-pipeline`.

3.  Click **Configure**.

4.  Under **Build Triggers**, check:

    **GitHub hook trigger for GITScm polling**

5.  Click **Save**.

You can leave Poll SCM enabled while testing the webhook. Once the
webhook works, disable Poll SCM if you want the webhook to be your only
trigger.

## 28. Why local Jenkins needs a tunnel

Your Jenkins URL is:

``` text
http://localhost:8080
```

GitHub's servers cannot reach your computer through `localhost`. For
this local lab, you can use ngrok to create a public HTTPS tunnel to
local port 8080.

Keep Jenkins authentication enabled. A public tunnel makes Jenkins
reachable from outside your computer. Use it only while testing and stop
it afterward.

## 29. Check ngrok on Windows

Open PowerShell. If ngrok is installed in `C:\Program Files\ngrok`, run:

``` powershell
cd "C:\Program Files\ngrok"
.\ngrok.exe version
```

If it is installed elsewhere, use that path. If it is not installed,
obtain it from the official ngrok website and follow its Windows
installation instructions.

## 30. Configure the ngrok authentication token

1.  Sign in to your ngrok account.
2.  Open its dashboard and find your authentication token.
3.  Run this command with your own token:

``` powershell
.\ngrok.exe config add-authtoken YOUR_NGROK_AUTH_TOKEN
```

Keep the token private. Do not put it in GitHub, the Jenkinsfile, or
screenshots.

## 31. Start the tunnel

Ensure Jenkins is running at `http://localhost:8080`. Then run:

``` powershell
.\ngrok.exe http 8080
```

Keep the PowerShell window open. ngrok should show a forwarding URL
similar to:

``` text
Forwarding  https://example-123.ngrok-free.app -> http://localhost:8080
```

Use the actual HTTPS URL shown by ngrok. Do not copy the example
literally. A temporary URL may change after ngrok restarts.

If ngrok reports an authentication error such as `ERR_NGROK_4018`, check
your token and account setup.

## 32. Add the webhook in GitHub

1.  Open the GitHub repository `gitmavenpractice`.
2.  Go to **Settings → Webhooks**.
3.  Click **Add webhook**.
4.  Fill in the following fields.

### Payload URL

Use your real ngrok HTTPS URL followed by `/github-webhook/`.

Example only:

``` text
https://example-123.ngrok-free.app/github-webhook/
```

The actual URL must use your current ngrok forwarding address and
include `/github-webhook/`.

### Content type

Select:

``` text
application/json
```

### Secret

For a basic temporary lab, you can leave this blank. For a more secure
setup, use a webhook secret and configure the integration to validate
it.

### Events

Select **Just the push event**.

Ensure **Active** is checked, then click **Add webhook**.

GitHub may send an initial ping event. Use a push event to verify that
the Jenkins pipeline starts.

## 33. Test the webhook

1.  Open **Settings → Webhooks** in GitHub.

2.  Select your webhook.

3.  Inspect **Recent Deliveries**.

4.  Make a small change to a tracked file.

5.  Commit and push:

    ``` powershell
    git add .
    git commit -m "Test GitHub webhook trigger"
    git push origin main
    ```

6.  Open Jenkins at `http://localhost:8080`.

7.  Check **Build History**.

Expected result: Jenkins starts a new build after the push event is
delivered and accepted.

### Webhook troubleshooting

**GitHub delivery fails or times out** - Make sure ngrok is still
running. - Confirm the payload URL uses the current HTTPS forwarding
URL. - Check that `/github-webhook/` is included. - Confirm Jenkins is
reachable locally on port 8080.

**GitHub delivery succeeds but no build starts** - Confirm **GitHub hook
trigger for GITScm polling** is checked. - Confirm the Jenkins job
points to the correct GitHub repository and `Jenkinsfile`. - Confirm the
commit is on the configured branch. - Review Jenkins logs and build
history.

**The webhook worked earlier but stopped** - The temporary ngrok URL may
have changed. Update the GitHub webhook to the current URL.

A webhook only triggers the pipeline. Maven, Docker, port binding, and
application errors can still cause the build itself to fail.



