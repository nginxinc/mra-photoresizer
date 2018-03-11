# NGINX Microservices Reference Architecture: PhotoResizer Service

This repository contains a Java application which is used to resize and upload images for the NGINX _Ingenious_ application. The 
_Ingenious_ application has been developed by the NGINX Professional Services team to provide a reference 
architecture for building your own microservices based application using NGINX as the service mesh. 

The default configuration for all the components of the MRA, including the PhotoResizer service, is to use the 
[Fabric Model Architecture](https://www.nginx.com/blog/microservices-reference-architecture-nginx-fabric-model/ "Fabric Model").
Instructions for using the [Router Mesh](https://www.nginx.com/blog/microservices-reference-architecture-nginx-router-mesh-model/) or 
[Proxy Model](https://www.nginx.com/blog/microservices-reference-architecture-nginx-proxy-model/) architectures will be made available in the future.

## Quick start
As a single service in the set of services which comprise the NGINX Microservices Reference Architecture application, _Ingenious_,
the PhotoResizer service is not meant to function as a standalone service. Once you have built the image, it can be deployed 
to a container engine along with the other components of the _Ingenious_ application, and then the application will be 
accessible via your browser. 

There are detailed instructions for building the service below, and in order to get started quickly, you can follow these simple 
instructions to quickly build the image.

0. (Optional) If you don't already have an NGINX Plus license, you can request a temporary developer license 
[here](https://www.nginx.com/developer-license/ "Developer License Form"). If you do have a license, then skip to the next step. 
1. Copy your licenses to the **<repository-path>/mra-photoresizer/nginx/ssl** directory
2. Run the command `docker build . -t <your-image-repo-name>/resizer:quickstart` where <image-repository-name> is the username
for where you store your Docker images
3. Once the image has been built, push it to your image repository with the command `docker push -t <your-image-repo-name>/resizer:quickstart`

At this point, you will have an image that is suitable for deployment on to a Kubernetes installation, and you can deploy the
image by creating YAML files and uploading them to your Kubernetes installation.

To build a customized image for different container engines or to set other options, please follow the directions below.

## Building a Customized Docker Image
The [Dockerfile](Dockerfile) for the PhotoResizer service is based on the openjdk:8-jdk-slim image, 
and installs NGINX open source or NGINX Plus. Note that NGINX Plus includes features which make discovery of other services possible, include additional load balancing algorithms, 
create persistent SSL/TLS connections, and provide advanced health check functionality.

Please refer to the comments in the [Dockerfile](Dockerfile) for details about each command which is
used to build the image. 

The command, or entrypoint, for the [Dockerfile](Dockerfile) is the [start.sh script](app/start.sh "Dockerfile entrypoint"). 
This script sets some local variables, then runs [java](https://docs.oracle.com/javase/tutorial/deployment/jar/run.html "Java") 
with _PhotoResizer.jar_ as a parameter. It also starts NGINX to send requests to a jetty server.
Configuration for the Java application is in the [PhotoResizer.yaml file](app/PhotoResizer.yaml "PhotoResizer YAML file")

### 1. Build options
The [Dockerfile](Dockerfile) sets some ENV arguments which are used when the image is built:

- **USE_NGINX_PLUS**  
    The default value is true. When this value is set to false, NGINX open source will be built in to the image and several 
    features, including service discovery and advanced load balancing will be disabled.
    See [installing nginx plus](#installing-nginx-plus)
    
- **USE_VAULT**  
    The default value is false. Setting this value to true will cause install-nginx.sh to look 
    for a file named vault_env.sh which contains the _VAULT_ADDR_ and _VAULT_TOKEN_ environment variables to
    retrieve NGINX Plus keys from a [vault](https://www.vaultproject.io/) server.
    
    ```
    #!/bin/bash
    export VAULT_ADDR=<your-vault-address>
    export VAULT_TOKEN=<your-vault-token>
    ```
    
    You must be certain to include the vault_env.sh file when _USE_VAULT_ is true. There is an entry in the [.gitignore](.gitignore)
    file for vault_env.sh
    
    In the future, we will release an article on our [blog](https://www.nginx.com/blog/) describing how to use vault with NGINX.    
    
- **CONTAINER_ENGINE**  
    The container engine used to run the images in a container. _CONTAINER_ENGINE_ can be one of the following values
     - kubernetes (default): to run on Kubernetes
        When the nginx.conf file is built, the [fabric_config_k8s.yaml](nginx/fabric_config_k8s.yaml) will be
        used to populate the open source version of the [nginx.conf template](nginx/nginx-plus-fabric.conf.j2)
     - mesos: to run on DC/OS
        When the nginx.conf file is built, the [fabric_config.yaml](nginx/fabric_config.yaml) will be
        used to populate the open source version of the [nginx.conf template](nginx/nginx-plus-fabric.conf.j2)  
     - local: to run in containers on the machine where the repository has been cloned
        When the nginx.conf file is built, the [fabric_config_local.yaml](nginx/fabric_config_local.yaml) will be
        used to populate the open source version of the [nginx.conf template](nginx/nginx-plus-fabric.conf.j2)                  
     
### 2. Decide whether to use NGINX Open Source or NGINX Plus
#### <a href="#" id="installing-nginx-oss"></a>Installing NGINX Open Source
Set the _USE_NGINX_PLUS_ property to false in the [Dockerfile](Dockerfile)
#### <a href="#" id="installing-nginx-plus"></a>Installing NGINX Plus
Before installing NGINX Plus, you'll need to obtain your license keys. If you do not already have a valid NGINX Plus subscription, you can request 
developer licenses [here](https://www.nginx.com/developer-license/ "Developer License Form") 

Set the _USE_NGINX_PLUS_ property to true in the [Dockerfile](Dockerfile)

By default _USE_VAULT_ is set to false, and you must manually copy your **nginx-repo.crt** and **nginx-repo.key** 
files to the **<path-to-repository>/mra-photoresizer/nginx/ssl/** directory.

Download the **nginx-repo.crt** and **nginx-repo.key** files for your NGINX Plus Developer License or subscription, and move them to the root directory of this project. You can then copy both of these files to the **/nginx/ssl** directory of each microservice using the command below:
```
cp nginx-repo.crt nginx-repo.key <repository>/nginx/ssl/
```
If _USE_VAULT_ is set to true, you must have installed a vault server and written the contents of the **nginx-repo.crt**
and **nginx-repo.key** file to vault server. Refer to the vault documentation for instructions configuring a vault server
and adding values to it. 

### 3. Decide which container engine to use
#### Set the _CONTAINER_ENGINE_ variable
As described above, the _CONTAINER_ENGINE_ environment variable must be set to one of the following three options.
The [install-nginx.sh](install-nginx.sh) file uses this value to determine which template file to use when populating the nginx.conf file.
- kubernetes 
- mesos 
- local

### 4. Build the image
Replace _&lt;your-image-repo-name&gt;_ with the username for where you store your Docker images, and execute the command below to build the image. The _&lt;tag&gt;_ argument is optional and defaults to **latest**
```
docker build . -t <your-image-repo-name>/resizer:<tag>
```

### 5. Modify the hosts file
The resizer service stores images using Amazon S3, the location of which is specified by the S3_URL environment variable.

For the purposes of demonstration, the application makes use of a simulated S3 service called [Fake S3](https://hub.docker.com/r/lphoward/fake-s3/), which runs as a separate service.

While running the Ingenious application locally, you must update your hosts file to match the value that is set for the S3_URL environment variable. By default, this value is http://fake-s3, so the hosts file should look like:
```
127.0.0.1    fake-s3
```

#### A note about Fake S3
[Fake S3](https://hub.docker.com/r/lphoward/fake-s3/) makes it simple to build and test applications which use Amazon S3. If Ingenious were in production, it would connect directly to an S3 service on AWS. Here are a couple of alternatives to Fake S3:
- [Moto](https://github.com/spulec/moto)
- [localstack](https://github.com/atlassian/localstack)

We haven't tested either of these and there are other options as well. If you successfully implement an alternative to Fake S3, let us know by creating a [pull request](https://github.com/nginxinc/mra-photoresizer/pulls).

### 6. Runtime environment variables
In order to run the image, some environment variables must be set so that they are available during runtime.

| Variable Name         | Description                                  | Example Value            |
| --------------------- | -------------------------------------------- | ------------------------ |
| AWS_ACCESS_KEY_ID     | Your AWS Key for S3                          | ABCD1234ABCD1234ABCD1234 |
| AWS_REGION            | The region where your S3 instance is running | us-west-1                |
| AWS_SECRET_ACCESS_KEY | Your AWS Secret Access Key                   | ABCD1234ABCD1234ABCD1234 |
| S3_URL                | The URL for the S3 host                      | http://fake-s3:4569      |
| REDIS_CACHE_PORT      | The Redis server port                        | 6379                     |
| REDIS_CACHE_URL       | The Redis server host name                   | redis                    |
| S3_BUCKET             | The name of the S3 bucket                    | mra-images               |

### 6. Service Endpoints
| Method | Endpoint    | Description                                      | Parameters |
| ------ | ----------- | ------------------------------------------------ | ---------- |
| POST   | /v1/resizer | Resize the image specified in the URL form input | url        |

### Disclaimer
In this service, the **nginx/ssl/dhparam.pem** file is provided for ease of setup. In production environments, it is highly recommended for secure key-exchange to replace this file with your own generated DH parameter.

You can generate your own **dhparam.pem** file using the command below:
```
openssl dhparam -out nginx/ssl/dhparam.pem 2048
```
