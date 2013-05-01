package controllers.java;

import play.*;
import play.mvc.*;

public class Application extends Controller {
    
  public static Result hello(String name) {
      return new Result {};
  }
  
  public static Result withIntegerArg(Integer i) {
    return new Result {};
  }
   
  public static void nonActionMethod(String name) {}
  
  public Result nonStaticMethod(String name) {
    return new Result {};
  }
  
  public static Result fieldIsNotValidAction = new Result {};
}