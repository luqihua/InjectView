package lu.inject;

/**
 * Created by Administrator on 2016/8/3.
 */
public interface ViewBinder<T> {
    void bind(T target, Object source);
}
