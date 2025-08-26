pluginManagement {
  repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven("https://maven.aliucord.com/snapshots")
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven("https://maven.aliucord.com/snapshots")
  }
}

rootProject.name = "moeGramX"
include(
  ":tdlib",

  ":vkryl:td",
  ":vkryl:android",
  ":vkryl:leveldb",
  ":vkryl:core",

  ":app"
)