package lu.inject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/8/3.
 */
public class FindVUtil {
    private static final String TAG = "FindVUtil";
    public static Map<String, ViewBinder> BINDS = new HashMap<>();

    public static void inject(Object target) {
        inject(target, null);
    }

    public static void inject(Object target, Object source) {
        String cName = target.getClass().getName();
        ViewBinder binder = null;
        try {
            binder = BINDS.get(cName);
            if (binder == null) {
                Class bindClass = Class.forName(cName + "$$ViewBinder");
                binder = (ViewBinder) bindClass.newInstance();
                BINDS.put(cName, binder);
            }
            binder.bind(target, source);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
