package com.example.admin.smartdiaper.permission;

import java.util.List;

/**
 * 权限处理接口
 */
public interface PermissionListener {
    void onGranted();//已授权回调
    void onDenied(List<String> deniedPermission);//未授权回调
}
