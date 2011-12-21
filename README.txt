Make sure you include this file (but don't push it to repo: use git rm --cached):
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


