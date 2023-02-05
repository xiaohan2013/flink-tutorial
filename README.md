###
=======
# flink-tutorial
flink tutorial for freshmen

### NetCat
nc -L -p 7777


### protoc
* maven-protoc
* https://blog.csdn.net/keenw/article/details/124458379
* protoc编译
* protoc-grpc 编译 https://blog.csdn.net/qq_24884867/article/details/119964401

### 一些问题
* idea maven 编译中文乱码. -DarchetypeCatalog=internal -Dfile.encoding=GBK
* maven-assembly-plugin，在父pom中定义即可，子pom可以被继承
* maven-enforcer-plugin, Enforcer可以在项目validate时，对项目环境进行检查。JDK的校验为例
* maven-jar-plugin，打包（jar）插件，设定 MAINFEST .MF文件的参数，比如指定运行的Main class、将依赖的jar包加入classpath中等等
* 


### flink docker
* docker build -f Dockerfile -t xiaozhu/flink-k8s-demo:v1.0.0 ..
* minikube build -f Dockerfile -t xiaozhu/flink-k8s-demo:v1.0.0 ..