package com.i69app.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.ContextCompat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.i69app.R;

public class NoDefaultSpinner extends AppCompatSpinner {

    public NoDefaultSpinner(Context context) {
        super(context);
    }

    public NoDefaultSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoDefaultSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(SpinnerAdapter orig) {
        final SpinnerAdapter adapter = newProxy(orig);

        super.setAdapter(adapter);

        try {
            final Method m = AdapterView.class.getDeclaredMethod("setNextSelectedPositionInt", int.class);
            m.setAccessible(true);
            m.invoke(this, -1);

            final Method n = AdapterView.class.getDeclaredMethod("setSelectedPositionInt", int.class);
            n.setAccessible(true);
            n.invoke(this, -1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SpinnerAdapter newProxy(SpinnerAdapter obj) {
        return (SpinnerAdapter) Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                new Class[]{SpinnerAdapter.class},
                new SpinnerAdapterProxy(obj, getContext(), getPrompt())
        );
    }


    /**
     * Intercepts getViewMethod() to display the prompt if position < 0
     */
    public static class SpinnerAdapterProxy implements InvocationHandler {

        SpinnerAdapter obj;
        Method getViewMethod;
        private Context context;
        private CharSequence prompt;

        SpinnerAdapterProxy(SpinnerAdapter obj, Context context, CharSequence prompt) {
            this.obj = obj;
            this.context = context;
            this.prompt = prompt;
            try {
                this.getViewMethod = SpinnerAdapter.class.getMethod("getView", int.class, View.class, ViewGroup.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
            try {
                return m.equals(getViewMethod) && (Integer) (args[0]) < 0
                        ? getView((int) args[0], (View) args[1], (ViewGroup) args[2])
                        : m.invoke(obj, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public int getPosition(String item) {
            return ((ArrayAdapter<String>) obj).getPosition(item);
        }

        View getView(int position, View convertView, ViewGroup parent) {
            if (position < 0) {
                View content = LayoutInflater.from(context)
                        .inflate(R.layout.item_no_default_spinner, parent, false);

                final TextView textView = content.findViewById(R.id.textSpinner);
                textView.setText(prompt);
                textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.edit_profile_text_hint_gray));
                return textView;
            }

            return obj.getView(position, convertView, parent);
        }
    }
}