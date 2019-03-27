package com.example.admin.smartdiaper.permission;

import android.content.Context;
import android.content.pm.PackageManager;

public class PermissionHelper {
    public static boolean hasPermission(Context context, String[] permissions){
        for(int i=0;i<permissions.length;i++){
            int perm=context.checkCallingOrSelfPermission(permissions[i]);
            if(perm!= PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }
}
