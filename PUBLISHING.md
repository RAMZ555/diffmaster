# How to Publish and Use DiffMaster

## Option 1: Local Development (Easiest)
To use `DiffMaster` in other projects on your **local machine**:

1. **Install to Local Repository**:
   Install the library to your local repository:
   ```bash
   # Windows (CMD)
   mvnw clean install
   
   # Mac/Linux/PowerShell
   ./mvnw clean install
   ```
   This builds the JAR and saves it to your local Maven cache (`~/.m2/repository`).

2. **Use in Another Project**:
   Add this to the `pom.xml` of your *application*:
   ```xml
   <dependency>
       <groupId>com.diffmaster</groupId>
       <artifactId>diffmaster</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

---

## Option 2: Share via GitHub (JitPack)
To share the library with others without setting up a complex Maven server, use **JitPack**.

1. **Push your code to GitHub**:
   - Create a repository on GitHub (e.g., `your-username/diffmaster`).
   - Push your code:
     ```bash
     git init
     git add .
     git commit -m "Initial commit"
     git branch -M main
     git remote add origin https://github.com/your-username/diffmaster.git
     git push -u origin main
     ```

2. **Create a Release**:
   - Go to your GitHub repo → **Releases** → **Create a new release**.
   - Tag it `1.0.0` (or `v1.0.0`).

3. **Use in Any Project**:
   In the `pom.xml` of the consuming project:

   **Add Repository:**
   ```xml
   <repositories>
       <repository>
           <id>jitpack.io</id>
           <url>https://jitpack.io</url>
       </repository>
   </repositories>
   ```

   **Add Dependency:**
   ```xml
   <dependency>
       <groupId>com.github.your-username</groupId>
       <artifactId>diffmaster</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```

---

## Option 3: Maven Central (Professional)
This is for public, production-grade libraries (like Spring or Gson). It requires:
- A generic JIRA account with Sonatype.
- GPG Keys for signing artifacts.
- Specific `pom.xml` configuration for distribution.

*Recommendation: Start with Option 1 or 2.*
