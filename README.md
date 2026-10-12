# CS4801-Git-Maven-Docker-Jenkins


## 5. Create `pom.xml`

Create a file named `pom.xml` in the project root. Paste and save:

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
        <maven.compiler.release>17</maven.compiler.release>
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
-   `maven.compiler.release` selects Java 17 language/API compatibility.
-   JUnit Jupiter is used for the unit test.
-   Maven Compiler Plugin compiles Java code.
-   Maven Surefire Plugin runs tests.

Validate the project:

``` powershell
mvn validate
```

Expected result: `BUILD SUCCESS`.

## 6. Create `App.java`

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

This is intentionally small. It starts a basic HTTP server and exposes
`/status`, which returns a text response. It also has a method that can
be tested by JUnit.

## 7. Create `AppTest.java`

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

This verifies that `getStatus()` returns the expected value.

## 8. Build and test with Maven

From the project root, run:

``` powershell
mvn clean package
```

Maven will:

1.  `clean` --- remove previous build output.
2.  `compile` --- compile the Java source.
3.  `test` --- run the JUnit test.
4.  `package` --- generate a JAR.

Look for output similar to:

``` text
Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

The JAR should be generated at:

``` text
target/gitmavenpractice-1.0-SNAPSHOT.jar
```

Do not proceed until the Maven build succeeds.

------------------------------------------------------------------------

# Part C --- Docker image and container

## 9. Create the `Dockerfile`

Create a file named exactly `Dockerfile` in the project root (no `.txt`
extension).

``` dockerfile
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/gitmavenpractice-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD ["java", "-cp", "app.jar", "com.example.studentmanagement.App"]
```

### What does this Dockerfile do?

-   `FROM` selects a Java 17 runtime base image.
-   `WORKDIR` sets the working directory inside the image.
-   `COPY` copies the Maven-built JAR into the image.
-   `EXPOSE 8080` documents the application port.
-   `CMD` starts the Java application when a container runs.

The Dockerfile expects Maven to have generated the JAR first.

## 10. Create `.gitignore`

Create `.gitignore` in the project root:

``` gitignore
target/
.idea/
.vscode/
*.iml
```

This excludes generated build output and local IDE settings from Git.
Maven will regenerate `target/` when needed.

## 11. Build the Docker image locally

Make sure Docker Desktop is running. From the project root, run:

``` powershell
docker build -t gitmavenpractice:1.0 .
```

Explanation:

-   `docker build`: builds an image.
-   `-t gitmavenpractice:1.0`: names and tags the image.
-   `.`: sends the current directory as the build context.

List the image:

``` powershell
docker images
```

Look for `gitmavenpractice` with tag `1.0`.

## 12. Run the container

Run:

``` powershell
docker run -d --name student-app -p 8081:8080 gitmavenpractice:1.0
```

Explanation:

-   `-d`: run in the background.
-   `--name student-app`: name the container.
-   `-p 8081:8080`: map host port 8081 to container port 8080.
-   `gitmavenpractice:1.0`: image to run.

Verify:

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

Stop and remove the old container:

``` powershell
docker rm -f student-app
```

Then run the `docker run` command again.

### If port 8081 is already in use

Use a different host port, such as:

``` powershell
docker run -d --name student-app -p 8083:8080 gitmavenpractice:1.0
```

Then browse to `http://localhost:8083/status`.

------------------------------------------------------------------------

# Part D --- Git and GitHub

## 13. Initialize Git

Make sure your terminal is in the `gitmavenpractice` root folder. Run:

``` powershell
git init
git add .
git commit -m "Add Java Maven application and Docker setup"
git branch -M main
```

If Git asks you to configure your identity, set your name and email:

``` powershell
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

Then retry the commit.

## 14. Connect to the GitHub repository

The remote repository is named `gitmavenpractice`.

Check whether a remote already exists:

``` powershell
git remote -v
```

If the correct GitHub URL is already listed, keep it. Otherwise add it:

``` powershell
git remote add origin https://github.com/YOUR_USERNAME/gitmavenpractice.git
```

Replace `YOUR_USERNAME` with your GitHub username.

Push the project:

``` powershell
git push -u origin main
```

If GitHub rejects the push because the remote already has a README or
other commits, do not use `git push --force` without checking the
situation. Review the error first.

## 15. Verify the repository

Open the repository on GitHub. The root should contain:

``` text
.gitignore
Dockerfile
Jenkinsfile     (created in Part E)
pom.xml
src/
```

Make a meaningful commit whenever you change project files. For example:

``` powershell
git add .
git commit -m "Describe the change"
git push origin main
```

------------------------------------------------------------------------

# Part E --- Create the Jenkins pipeline

## 16. Create `Jenkinsfile`

Create a file named exactly `Jenkinsfile` in the project root.

Use this Windows-oriented declarative pipeline:

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

1.  **Checkout** --- gets the source code from the Git repository
    configured in the Jenkins job.
2.  **Build and Test** --- runs Maven compilation, tests, and packaging.
3.  **Build Docker Image** --- creates the image using the JAR produced
    by Maven.
4.  **Deploy** --- removes an existing container if present, then runs
    the new container.

The cleanup command is written to tolerate a missing container. The
subsequent Docker run still fails if Docker is unavailable or the port
cannot be bound.

### Important Windows/Jenkins configuration

The pipeline uses `bat` commands. It assumes that the Jenkins agent is
Windows and that the Windows account running Jenkins can access:

-   `git`
-   `mvn`
-   `docker`

A tool working in your personal terminal does not guarantee that it is
on the Jenkins service account's `PATH`. If Jenkins reports that `mvn`
or `docker` is not recognized, check the Jenkins service account and
system environment variables.

## 17. Commit and push the Jenkinsfile

Run:

``` powershell
git add Jenkinsfile
git commit -m "Add Jenkins CI/CD pipeline"
git push origin main
```

If you already committed the file, Git may report that there is nothing
to commit. That is fine.

------------------------------------------------------------------------

# Part F --- Configure the Jenkins job

## 18. Open Jenkins

Open:

``` text
http://localhost:8082
```

Sign in.

## 19. Create the Pipeline job

1.  Click **New Item**.

2.  Enter:

    ``` text
    gitmavenpractice-pipeline
    ```

3.  Select **Pipeline**.

4.  Click **OK**.

## 20. Configure the job to fetch from GitHub

Scroll to the **Pipeline** section near the bottom of the configuration
page.

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

For a public repository, read access usually needs no credentials. For a
private repository, configure suitable Git credentials in Jenkins.

Click **Save**.

## 21. Run the first build

1.  Open `gitmavenpractice-pipeline`.
2.  Click **Build Now**.
3.  Click the build number in **Build History**.
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

The Jenkins job uses port 8081 on the host, while the Java server
listens on port 8080 inside the container.

------------------------------------------------------------------------

# Part G --- Automatically trigger builds with Poll SCM

## 22. What Poll SCM does

Poll SCM makes Jenkins periodically check the configured Git repository.
If it detects new commits, Jenkins schedules a build. If there are no
relevant changes, it normally does not start a new build.

## 23. Configure Poll SCM

1.  Open Jenkins.

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
scheduling does not support a 30-second interval.

The five cron fields are:

``` text
MINUTE HOUR DAY-OF-MONTH MONTH DAY-OF-WEEK
```

`* * * * *` means every minute.

## 24. Test Poll SCM

Make a small change to a tracked file, such as the response string in
`App.java`. Then run:

``` powershell
git add .
git commit -m "Test Poll SCM trigger"
git push origin main
```

Wait for the next poll and inspect Jenkins **Build History**. Allow a
couple of minutes for the scheduled poll and build to occur.

Expected result: a new build starts without clicking **Build Now**.

### Poll SCM troubleshooting

-   Confirm Poll SCM is checked and saved.
-   Confirm the repository URL and branch are correct.
-   Confirm the commit was pushed to GitHub.
-   Check Jenkins logs for Git or credential errors.
-   Ensure the job is configured as **Pipeline script from SCM**.

------------------------------------------------------------------------

# Part H --- Automatically trigger builds with a GitHub webhook

## 25. What a webhook does

A webhook is event-driven. After a push, GitHub sends a request to
Jenkins. If Jenkins receives and accepts the event, it checks the
repository and can schedule a build.

Unlike Poll SCM, a webhook does not need Jenkins to wait for the next
polling interval.

## 26. Enable the webhook trigger in Jenkins

1.  Open Jenkins at `http://localhost:8082`.

2.  Open `gitmavenpractice-pipeline`.

3.  Click **Configure**.

4.  Under **Build Triggers**, check:

    **GitHub hook trigger for GITScm polling**

5.  Click **Save**.

You can leave Poll SCM enabled while testing the webhook. Once the
webhook works, disable Poll SCM if you want the webhook to be your only
trigger.

## 27. Why a local Jenkins server needs a tunnel

Your current Jenkins URL is:

``` text
http://localhost:8082
```

GitHub's servers cannot reach your computer through `localhost`. For
this local lab, use a public HTTPS tunnel such as ngrok to forward
requests to port 8082.

Keep Jenkins authentication enabled. A public tunnel makes the Jenkins
endpoint reachable from outside your computer. Use it only while testing
and stop it afterward.

## 28. Check ngrok on Windows

Open PowerShell. If ngrok is already installed in
`C:\Program Files\ngrok`, run:

``` powershell
cd "C:\Program Files\ngrok"
.\ngrok.exe version
```

If it is installed elsewhere, use that path. If it is not installed,
obtain it from the official ngrok site and follow its Windows
installation instructions.

## 29. Configure the ngrok token

1.  Sign in to your ngrok account.
2.  Open its dashboard and find your authentication token.
3.  Run this command with your own token:

``` powershell
.\ngrok.exe config add-authtoken YOUR_NGROK_AUTH_TOKEN
```

Keep the token private. Do not put it in GitHub, the Jenkinsfile, or
screenshots.

## 30. Start the tunnel

Ensure Jenkins is running at `http://localhost:8082`. Then run:

``` powershell
.\ngrok.exe http 8082
```

Keep the PowerShell window open. ngrok should show a forwarding URL
similar to:

``` text
Forwarding  https://example-123.ngrok-free.app -> http://localhost:8082
```

Use the actual HTTPS URL displayed in your terminal. Do not copy the
example literally. A temporary URL may change after you restart ngrok.

If ngrok reports an authentication error such as `ERR_NGROK_4018`, check
your token and account setup.

## 31. Add the webhook in GitHub

1.  Open the GitHub repository `gitmavenpractice`.
2.  Go to **Settings → Webhooks**.
3.  Click **Add webhook**.
4.  Fill in these values.

### Payload URL

Use the real ngrok URL followed by `/github-webhook/`.

Example only:

``` text
https://example-123.ngrok-free.app/github-webhook/
```

The URL must use the actual forwarding address and include
`/github-webhook/`.

### Content type

Select:

``` text
application/json
```

### Secret

For a basic temporary lab, you can leave this blank. A more secure setup
can use a webhook secret if the integration is configured to validate
it.

### Events

Select **Just the push event**.

Ensure **Active** is checked, then click **Add webhook**.

GitHub may send an initial ping event. Use a push event to test whether
the Jenkins pipeline actually starts.

## 32. Test the webhook

1.  Open **Settings → Webhooks** in GitHub.

2.  Select the webhook.

3.  Inspect **Recent Deliveries**.

4.  Make a small change to a tracked file.

5.  Commit and push:

    ``` powershell
    git add .
    git commit -m "Test GitHub webhook trigger"
    git push origin main
    ```

6.  Open Jenkins and inspect **Build History**.

Expected result: Jenkins starts a new build after the push event is
delivered and accepted.

### Webhook troubleshooting

**GitHub delivery times out or fails** - Make sure ngrok is still
running. - Confirm the payload URL uses the current HTTPS forwarding
URL. - Check that `/github-webhook/` is included. - Confirm Jenkins is
reachable locally on port 8082.

**GitHub delivery succeeds but no build starts** - Confirm **GitHub hook
trigger for GITScm polling** is checked. - Confirm the Jenkins job
points to the correct GitHub repository and `Jenkinsfile`. - Confirm the
commit is on the configured branch. - Review Jenkins logs and build
history.

**The webhook worked earlier but stopped** - A temporary ngrok URL may
have changed. Update the GitHub webhook to the current URL.

A webhook only triggers the pipeline. Maven, Docker, ports, and
application errors can still cause the resulting build to fail.

------------------------------------------------------------------------

