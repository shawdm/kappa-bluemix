# kappa-bluemix

## Prerequisites
To build and run the sample, you must have the following installed:

* [git](https://git-scm.com/)
* [Gradle](https://gradle.org/)
* Java 7+
* [Message Hub Service Instance](https://console.ng.bluemix.net/catalog/services/message-hub/) provisioned in [IBM Bluemix](https://console.ng.bluemix.net/)


## General Steps

## Building the Sample
Install the project using gradle:
```shell
gradle build war
 ```

You should see a directory called `target` created in your project home directory. A WAR file is created under `target/defaultServer`, as well as a copy of the server.xml file.

## Deployment Prerequisites
To deploy applications using the IBM WebSphere Application Server Liberty Buildpack, you are required to accept the IBM Liberty license and IBM JRE license by following the instructions below:

1. Move `manifest.yml.template` to `manifest.yml`
2. Read the current IBM [Liberty-License][] and the current IBM [JVM-License][].
3. Extract the `D/N: <License code>` from the Liberty-License and JVM-License.
4. Add the following environment variables and extracted license codes to the `manifest.yml` file in the directory from which you push your application. For further information on the format of
the `manifest.yml` file refer to the [manifest documentation][].

```yaml
env:
    IBM_JVM_LICENSE: <jvm license code>
    IBM_LIBERTY_LICENSE: <liberty license code>
```

__Note:__ Please use domain *eu-gb.mybluemix.net* within the manifest.yml if you are using Bluemix within London (console.eu-gb.bluemix.net). You may also need to use a unique hostname e.g. *host: JohnsSampleLibertyApp*

## Deploy the Sample to Bluemix
Now we can push the app to Bluemix:
```shell
cf push
 ```


[Liberty-License]: http://public.dhe.ibm.com/ibmdl/export/pub/software/websphere/wasdev/downloads/wlp/8.5.5.7/lafiles/runtime/en.html
[JVM-License]: http://www14.software.ibm.com/cgi-bin/weblap/lap.pl?la_formnum=&li_formnum=L-JWOD-9SYNCP&title=IBM%C2%AE+SDK%2C+Java+Technology+Edition%2C+Version+8.0&l=en
[manifest documentation]: http://docs.cloudfoundry.org/devguide/deploy-apps/manifest.html


# TODO
* install liberty and push after build for test
* use search topic for producer and consumer
* move existing kappa code across
