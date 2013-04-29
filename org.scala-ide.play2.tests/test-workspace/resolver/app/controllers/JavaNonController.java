package controllers;

import play.mvc.Controller;
import play.mvc.Result;

public class JavaNonController extends Controller {

  // not static
  public Result index() {
    /*!*/
    return null;
  }

  // not returning resultResult
  public static String index2(long lng) {
    /*!*/
    return null;
  }
  
  // not public
  static Result index3(String str, int id) {
    /*!*/
    return null;
  }
}
