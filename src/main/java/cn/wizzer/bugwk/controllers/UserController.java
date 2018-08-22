package cn.wizzer.bugwk.controllers;

import cn.wizzer.bugwk.commons.base.Result;
import cn.wizzer.bugwk.commons.filter.MyCrossOriginFilter;
import cn.wizzer.bugwk.commons.filter.MyRoleFilter;
import cn.wizzer.bugwk.modles.User;
import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.QueryResult;
import org.nutz.dao.pager.Pager;
import org.nutz.ioc.loader.annotation.Inject;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Lang;
import org.nutz.lang.Times;
import org.nutz.lang.random.R;
import org.nutz.log.Log;
import org.nutz.log.Logs;
import org.nutz.mvc.adaptor.JsonAdaptor;
import org.nutz.mvc.annotation.*;

import java.util.List;

/**
 * Created by wizzer on 2018.08
 */
@IocBean
@At("/platform/user")
@Filters({@By(type = MyCrossOriginFilter.class),@By(type = MyRoleFilter.class)})
public class UserController {
    private static final Log log = Logs.get();
    @Inject
    private Dao dao;

    @At("/add")
    @Ok("json")
    @AdaptBy(type = JsonAdaptor.class)
    public Object add(@Param("::") User user) {
        try {
            if (dao.count(User.class, Cnd.where("loginname", "=", user.getLoginname())) > 0) {
                return Result.error("用户已存在");
            }
            String salt = R.UU32();
            user.setSalt(salt);
            user.setLoginpass(Lang.md5(user.getLoginname() + salt));
            user.setCreateAt(Times.getTS());
            dao.insert(user);
            return Result.success();
        } catch (Exception e) {
            return Result.error();
        }
    }

    @At("/data")
    @Ok("json:{locked:'loginpass|salt',ignoreNull:false}")
    @AdaptBy(type = JsonAdaptor.class)
    public Object data(@Param(value = "size", df = "10") int size, @Param(value = "page", df = "1") int page) {
        try {
            Cnd cnd = Cnd.NEW();
            cnd.asc("loginname");
            Pager pager = new Pager();
            pager.setPageNumber(page);
            pager.setPageSize(size);
            pager.setRecordCount(dao.count(User.class, cnd));
            List<User> list = dao.query(User.class, cnd, pager);
            return Result.success(new QueryResult(list, pager));
        } catch (Exception e) {
            return Result.error();
        }
    }
}