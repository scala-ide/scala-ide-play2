package controllers.java;

import play.*;
import play.mvc.*;

public abstract class AbstractApplication extends Controller {
  public static Result hello(String name) {
      return new Result {};
  }
}