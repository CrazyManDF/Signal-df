pluginManagement {
    repositories {
//        maven(url="https://maven.aliyun.com/repository/gradle-plugin")
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven(url="https://maven.aliyun.com/repository/public")
        google()
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://raw.githubusercontent.com/signalapp/maven/master/sqlcipher/release/")
            content {
                includeGroupByRegex("org\\.signal.*")
            }
        }
        maven {
            url = uri("https://dl.cloudsmith.io/qxAgwaeEE1vN8aLU/mobilecoin/mobilecoin/maven/")
        }
//        jcenter {
//            content {
//                includeVersion("mobi.upod", "time-duration-picker", "1.1.3")
//            }
//        }
    }
}

rootProject.name = "Signal-Test"
include(":app")
include(":core-util-jvm")
include(":core-util")
include(":libsignal-service")
