package cn.cerc.mis.core;

import cn.cerc.core.IHandle;

import java.util.List;

public interface IAppMenu {

    // 返回系统菜单定义
    MenuItem getItem(String menuId);

    // 返回系统所有的Module
    List<MenuItem> getModule(IHandle handle);

    // 返回指定父菜单下的所有子菜单
    List<MenuItem> getList(IHandle handle, String parentId, boolean security);
}
