1) To use protoc, go to top of project and type 
protoc -I=src/com/lvl6/proto/ --java_out=src/ src/com/lvl6/proto/*.proto

2) Useful commands if you know what they do
git ls-files --deleted -z | xargs -0 git rm

3) Make sure you include this file (but don't push it to repo (it's already in .gitignore, if right path)):
DBProperties.java
in com.lvl6.properties package.

Should look like this:
package com.lvl6.properties;

public class DBProperties {
  
  public static final String USER = <fill in your info>
  public static final String PASSWORD = <fill in your info>
  public static final String SERVER = <fill in your info>
  public static final String DATABASE = <fill in your info>
    
}

