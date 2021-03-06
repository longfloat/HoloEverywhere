
package org.holoeverywhere.app;

import android.content.Context;
import android.support.v7.internal.view.menu.ContextMenuDecorView.ContextMenuListenersProvider;
import android.support.v7.internal.view.menu.ContextMenuListener;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import org.holoeverywhere.HoloEverywhere;
import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.R;
import org.holoeverywhere.internal.WindowDecorView;
import org.holoeverywhere.util.WeaklyMap;

import java.util.Map;

public class Dialog extends android.app.Dialog implements ContextMenuListener,
        ContextMenuListenersProvider {
    private Map<View, ContextMenuListener> mContextMenuListeners;
    private WindowDecorView mDecorView;

    public Dialog(Context context) {
        this(context, 0);
    }

    public Dialog(Context context, boolean cancelable,
                  OnCancelListener cancelListener) {
        this(context);
        setCancelable(cancelable);
        setOnCancelListener(cancelListener);
    }

    public Dialog(Context context, int theme) {
        this(context, checkTheme(context, theme), -1);
        setCancelable(true);
    }

    private Dialog(Context context, int theme, int fallback) {
        super(new ContextThemeWrapperPlus(context, theme), theme);
    }

    private static final int checkTheme(Context context, int theme) {
        if (theme >= 0x01000000) {
            return theme;
        }
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.dialogTheme, value, true);
        if (value.resourceId > 0) {
            return value.resourceId;
        }
        return R.style.Holo_Theme_Dialog;
    }

    @Override
    public void onContextMenuClosed(ContextMenu menu) {

    }

    @Override
    public void addContentView(View view, LayoutParams params) {
        if (requestDecorView(view, params, -1)) {
            mDecorView.addView(view, params);
        }
    }

    @Override
    public ContextMenuListener getContextMenuListener(View view) {
        if (mContextMenuListeners == null) {
            return null;
        }
        return mContextMenuListeners.get(view);
    }

    @Override
    public LayoutInflater getLayoutInflater() {
        return LayoutInflater.from(getContext());
    }

    @Override
    public void registerForContextMenu(View view) {
        if (HoloEverywhere.WRAP_TO_NATIVE_CONTEXT_MENU) {
            super.registerForContextMenu(view);
        } else {
            registerForContextMenu(view, this);
        }
    }

    public void registerForContextMenu(View view, ContextMenuListener listener) {
        if (mContextMenuListeners == null) {
            mContextMenuListeners = new WeaklyMap<View, ContextMenuListener>();
        }
        mContextMenuListeners.put(view, listener);
    }

    private boolean requestDecorView(View view, LayoutParams params, int layoutRes) {
        if (mDecorView != null) {
            return true;
        }
        mDecorView = new WindowDecorView(getContext());
        mDecorView.setId(android.R.id.content);
        mDecorView.setProvider(this);
        if (view != null) {
            mDecorView.addView(view, params);
        } else if (layoutRes > 0) {
            getLayoutInflater().inflate(layoutRes, mDecorView, true);
        }
        getWindow().setContentView(mDecorView, new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return false;
    }

    @Override
    public void setCancelable(boolean flag) {
        super.setCancelable(flag);
        setCanceledOnTouchOutside(flag);
    }

    @Override
    public void setContentView(int layoutResID) {
        if (requestDecorView(null, null, layoutResID)) {
            mDecorView.removeAllViewsInLayout();
            getLayoutInflater().inflate(layoutResID, mDecorView, true);
        }
    }

    @Override
    public void setContentView(View view) {
        setContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        if (requestDecorView(view, params, -1)) {
            mDecorView.removeAllViewsInLayout();
            mDecorView.addView(view, params);
        }
    }

    @Override
    public void unregisterForContextMenu(View view) {
        if (HoloEverywhere.WRAP_TO_NATIVE_CONTEXT_MENU) {
            super.unregisterForContextMenu(view);
        } else {
            if (mContextMenuListeners != null) {
                mContextMenuListeners.remove(view);
            }
        }
    }
}
