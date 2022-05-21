package com.i69app.ui.views.chipcloud;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.List;

import com.i69app.R;
import com.i69app.data.models.IdWithValue;
import com.i69app.utils.UtilsKt;

public class ChipCloud extends FlowLayout implements ChipListener {

    private final Context context;
    private int chipHeight;
    private int selectedFontColor = -1;
    private int unselectedFontColor = -1;
    private int chipCounter = -1;
    private int selectTransitionMS = 750;
    private int deselectTransitionMS = 500;
    private Mode mode = Mode.SINGLE;
    private Gravity gravity = Gravity.LEFT;
    private Typeface typeface;
    private final ArrayList<IdWithValue> chipsList = new ArrayList<>();
    private boolean allCaps;
    private int textSizePx = -1;
    private int verticalSpacing;
    private int minHorizontalSpacing;
    private ChipListener chipListener;
    private int selectedInt = 0;
    private ChipSelectedCount chipCountListener = () -> selectedInt;

    public ChipCloud(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ChipCloud(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ChipCloud, 0, 0);
        int arrayReference = -1;
        try {
            selectedFontColor = a.getColor(R.styleable.ChipCloud_selectedFontColor, -1);
            unselectedFontColor = a.getColor(R.styleable.ChipCloud_deselectedFontColor, -1);
            chipCounter = a.getInt(R.styleable.ChipCloud_chipMaxSelectedCounter, -1);
            selectTransitionMS = a.getInt(R.styleable.ChipCloud_selectTransitionMS, 750);
            deselectTransitionMS = a.getInt(R.styleable.ChipCloud_deselectTransitionMS, 500);
            String typefaceString = a.getString(R.styleable.ChipCloud_typeface);
            if (typefaceString != null) {
                typeface = Typeface.createFromAsset(getContext().getAssets(), typefaceString);
            }
            textSizePx = a.getDimensionPixelSize(R.styleable.ChipCloud_textSize,
                    getResources().getDimensionPixelSize(R.dimen.default_textsize));
            allCaps = a.getBoolean(R.styleable.ChipCloud_allCaps, false);
            int selectMode = a.getInt(R.styleable.ChipCloud_selectMode, 1);
            switch (selectMode) {
                case 0:
                    mode = Mode.SINGLE;
                    break;
                case 1:
                    mode = Mode.MULTI;
                    break;
                case 2:
                    mode = Mode.REQUIRED;
                    break;
                case 3:
                    mode = Mode.NONE;
                    break;
                default:
                    mode = Mode.SINGLE;
            }
            int gravityType = a.getInt(R.styleable.ChipCloud_gravity, 0);
            switch (gravityType) {
                case 0:
                    gravity = Gravity.LEFT;
                    break;
                case 1:
                    gravity = Gravity.RIGHT;
                    break;
                case 2:
                    gravity = Gravity.CENTER;
                    break;
                case 3:
                    gravity = Gravity.STAGGERED;
                    break;
                default:
                    gravity = Gravity.LEFT;
                    break;
            }
            minHorizontalSpacing = a.getDimensionPixelSize(R.styleable.ChipCloud_minHorizontalSpacing,
                    getResources().getDimensionPixelSize(R.dimen.min_horizontal_spacing));
            verticalSpacing = a.getDimensionPixelSize(R.styleable.ChipCloud_verticalSpacing,
                    getResources().getDimensionPixelSize(R.dimen.vertical_spacing));
            arrayReference = a.getResourceId(R.styleable.ChipCloud_labels, -1);

        } finally {
            a.recycle();
        }

        init();
    }

    @Override
    protected int getMinimumHorizontalSpacing() {
        return minHorizontalSpacing;
    }

    public void setMinimumHorizontalSpacing(int spacingInPx) {
        this.minHorizontalSpacing = spacingInPx;
    }

    @Override
    protected int getVerticalSpacing() {
        return verticalSpacing;
    }

    public void setVerticalSpacing(int spacingInPx) {
        this.verticalSpacing = spacingInPx;
    }

    @Override
    protected Gravity getGravity() {
        return gravity;
    }

    public void setGravity(Gravity gravity) {
        this.gravity = gravity;
    }

    private void init() {
        chipHeight = getResources().getDimensionPixelSize(R.dimen.material_chip_height);
    }

    public ArrayList<IdWithValue> getChipsList() {
        return chipsList;
    }

    public void setChipMaxSelectionCounter(int chipCounter) {
        this.chipCounter = chipCounter;
    }

    public void setSelectedFontColor(int selectedFontColor) {
        this.selectedFontColor = selectedFontColor;
    }

    public void setUnselectedFontColor(int unselectedFontColor) {
        this.unselectedFontColor = unselectedFontColor;
    }

    public void setSelectTransitionMS(int selectTransitionMS) {
        this.selectTransitionMS = selectTransitionMS;
    }

    public void setDeselectTransitionMS(int deselectTransitionMS) {
        this.deselectTransitionMS = deselectTransitionMS;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        for (int i = 0; i < getChildCount(); i++) {
            Chip chip = (Chip) getChildAt(i);
            chip.deselect();
            chip.setLocked(false);
        }
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
    }

    public void setTextSize(int textSize) {
        this.textSizePx = textSize;
    }

    public void setAllCaps(boolean isAllCaps) {
        this.allCaps = isAllCaps;
    }

    public void setChipListener(ChipListener chipListener) {
        this.chipListener = chipListener;
    }

    public void addChips(List<IdWithValue> chipList) {
        for (IdWithValue chipItem : chipList) {
            addChip(chipItem);
        }
    }

    public void addChip(IdWithValue chipItem) {
        String value;
        if (UtilsKt.isCurrentLanguageFrench()) {
            value = chipItem.getValueFr();
        } else {
            value = chipItem.getValue();
        }
        Chip chip = new Chip.ChipBuilder()
                .index(getChildCount())
                .tag(chipItem.getId())
                .label(value)
                .typeface(typeface)
                .textSize(textSizePx)
                .allCaps(allCaps)
                .selectedFontColor(selectedFontColor)
                .unselectedFontColor(unselectedFontColor)
                .selectTransitionMS(selectTransitionMS)
                .chipCounter(chipCounter)
                .deselectTransitionMS(deselectTransitionMS)
                .chipHeight(chipHeight)
                .chipListener(this)
                .chipCounterListener(chipCountListener)
                .mode(mode)
                .build(context);

        addView(chip);
        chipsList.add(chipItem);
    }

    public void setSelectedChip(int tag) {
        Chip chip = findViewWithTag(tag);
        if (chip!=null)chip.select();
//        if (mode == Mode.REQUIRED) {
//            for (int i = 0; i < getChildCount(); i++) {
//                Chip chipp = (Chip) getChildAt(i);
//                chipp.setLocked(i == index);
//            }
//        }
    }

    @Override
    public void chipSelected(int index) {
        switch (mode) {
            case SINGLE:
            case REQUIRED:
                for (int i = 0; i < getChildCount(); i++) {
                    Chip chip = (Chip) getChildAt(i);
                    if (i == index) {
                        if (mode == Mode.REQUIRED) chip.setLocked(true);
                    } else {
                        chip.deselect();
                        chip.setLocked(false);
                    }
                }
                break;
            default:
                //
        }
        ++selectedInt;
        if (chipListener != null) {
            chipListener.chipSelected(index);
        }
    }

    @Override
    public void chipDeselected(int index) {
        --selectedInt;
        if (chipListener != null) {
            chipListener.chipDeselected(index);
        }
    }

    @Override
    public void chipMaxCountLimit(int count) {
        if (chipListener != null) {
            chipListener.chipMaxCountLimit(count);
        }
    }

    public boolean isSelected(int index) {
        if (index > 0 && index < getChildCount()) {
            Chip chip = (Chip) getChildAt(index);
            return chip.isSelected();
        }
        return false;
    }

    public enum Mode {
        SINGLE, MULTI, REQUIRED, NONE
    }

    public interface ChipSelectedCount {
        int count();
    }

//    //Apparently using the builder pattern to configure an object has been labelled a 'Bloch Builder'.
//    public static class Configure {
//        private ChipCloud chipCloud;
//        private int selectedColor = -1;
//        private int selectedFontColor = -1;
//        private int deselectedColor = -1;
//        private int deselectedFontColor = -1;
//        private int selectTransitionMS = -1;
//        private int deselectTransitionMS = -1;
//        private Mode mode = null;
//        private ArrayList<IdWithValue> labels = new ArrayList<>();
//        private ChipListener chipListener;
//        private Gravity gravity = null;
//        private Typeface typeface;
//        private Boolean allCaps = null;
//        private int textSize = -1;
//        private int minHorizontalSpacing = -1;
//        private int verticalSpacing = -1;
//
//        public Configure chipCloud(ChipCloud chipCloud) {
//            this.chipCloud = chipCloud;
//            return this;
//        }
//
//        public Configure mode(Mode mode) {
//            this.mode = mode;
//            return this;
//        }
//
//        public Configure selectedColor(int selectedColor) {
//            this.selectedColor = selectedColor;
//            return this;
//        }
//
//        public Configure selectedFontColor(int selectedFontColor) {
//            this.selectedFontColor = selectedFontColor;
//            return this;
//        }
//
//        public Configure deselectedColor(int deselectedColor) {
//            this.deselectedColor = deselectedColor;
//            return this;
//        }
//
//        public Configure deselectedFontColor(int deselectedFontColor) {
//            this.deselectedFontColor = deselectedFontColor;
//            return this;
//        }
//
//        public Configure selectTransitionMS(int selectTransitionMS) {
//            this.selectTransitionMS = selectTransitionMS;
//            return this;
//        }
//
//        public Configure deselectTransitionMS(int deselectTransitionMS) {
//            this.deselectTransitionMS = deselectTransitionMS;
//            return this;
//        }
//
//        public Configure labels(ArrayList<IdWithValue> labels) {
//            this.labels = labels;
//            return this;
//        }
//
//        public Configure chipListener(ChipListener chipListener) {
//            this.chipListener = chipListener;
//            return this;
//        }
//
//        public Configure gravity(Gravity gravity) {
//            this.gravity = gravity;
//            return this;
//        }
//
//        public Configure typeface(Typeface typeface) {
//            this.typeface = typeface;
//            return this;
//        }
//
//        /**
//         * @param textSize value in pixels as obtained from @{@link android.content.res.Resources#getDimensionPixelSize(int)}
//         */
//        public Configure textSize(int textSize) {
//            this.textSize = textSize;
//            return this;
//        }
//
//        public Configure allCaps(boolean isAllCaps) {
//            this.allCaps = isAllCaps;
//            return this;
//        }
//
//        public Configure minHorizontalSpacing(int spacingInPx) {
//            this.minHorizontalSpacing = spacingInPx;
//            return this;
//        }
//
//        public Configure verticalSpacing(int spacingInPx) {
//            this.verticalSpacing = spacingInPx;
//            return this;
//        }
//
//        public void build() {
//            chipCloud.removeAllViews();
//            if (mode != null) chipCloud.setMode(mode);
//            if (gravity != null) chipCloud.setGravity(gravity);
//            if (typeface != null) chipCloud.setTypeface(typeface);
//            if (textSize != -1) chipCloud.setTextSize(textSize);
//            if (allCaps != null) chipCloud.setAllCaps(allCaps);
//            if (selectedFontColor != -1) chipCloud.setSelectedFontColor(selectedFontColor);
//            if (deselectedFontColor != -1) chipCloud.setUnselectedFontColor(deselectedFontColor);
//            if (selectTransitionMS != -1) chipCloud.setSelectTransitionMS(selectTransitionMS);
//            if (deselectTransitionMS != -1) chipCloud.setDeselectTransitionMS(deselectTransitionMS);
//            if (minHorizontalSpacing != -1)
//                chipCloud.setMinimumHorizontalSpacing(minHorizontalSpacing);
//            if (verticalSpacing != -1) chipCloud.setVerticalSpacing(verticalSpacing);
//            chipCloud.setChipListener(chipListener);
//            chipCloud.addChips(labels);
//        }
//
//        public void update() {
//            int childCount = chipCloud.getChildCount();
//            for (int i = 0; i < childCount; i++) {
//                Chip chip = (Chip) chipCloud.getChildAt(i);
//                chip.setText(labels.get(i).getValue());
//                chip.invalidate();
//            }
//            chipCloud.invalidate();
//            chipCloud.requestLayout();
//        }
//    }
}
