import play.*;
import play.mvc.*;

public class RootStaticJavaApplication extends Controller {
	public static Result simple() {
		return new Result() {};
	}
}
