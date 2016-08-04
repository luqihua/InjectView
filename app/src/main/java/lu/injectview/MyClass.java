package lu.injectview;

import lu.activity.Main2Activity;

/**
 * Created by Administrator on 2016/8/4.
 */
public class MyClass {


    public static void main(String[] args) {
        String str = "com.android.view.View";
        String name = Main2Activity.class.getName();
        String nameStr = name.substring(name.lastIndexOf(".") + 1);
        System.out.print(nameStr);

    }
}
