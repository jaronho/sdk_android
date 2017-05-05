package com.jaronho.sdk.utils.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Author:  Administrator
 * Date:    2017/4/27
 * Brief:   RefreshLayout
 */

public class RefreshView extends RelativeLayout implements SpringLayout.Listener {
    private SpringLayout mViewLayout = null;
    private RecyclerView mView = null;
    private RelativeLayout mHeader = null;
    private RelativeLayout mFooter = null;
    private boolean mIsDragRight = false;
    private boolean mIsDragLeft = false;
    private boolean mIsDragBottom = false;
    private boolean mIsDragTop = false;
    private Wrapper mHeaderWrapper = null;
    private Wrapper mFooterWrapper = null;
    private View mHeaderView = null;
    private View mFooterView = null;
    private int mHeaderViewSize = 0;
    private int mFooterViewSize = 0;
    private float mHeaderOffset = 0;
    private float mFooterOffset = 0;

    public RefreshView(Context context) {
        super(context);
        initialize();
    }

    public RefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public RefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RefreshView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && null != mHeaderView && mHeaderViewSize <= 0) {
            mHeaderViewSize = mViewLayout.isHorizontal() ? mHeaderView.getMeasuredWidth() : mHeaderView.getMeasuredHeight();
            setHeaderMargin(-mHeaderViewSize);
        }
        if (changed && null != mFooterView && mFooterViewSize <= 0) {
            mFooterViewSize = mViewLayout.isHorizontal() ? mFooterView.getMeasuredWidth() : mFooterView.getMeasuredHeight();
            setFooterMargin(-mFooterViewSize);
        }
    }

    @Override
    public boolean isCanScroll(boolean isHorizontal, boolean isForward) {
        mIsDragRight = mIsDragLeft = mIsDragBottom = mIsDragTop = false;
        if (isHorizontal) { // 水平滑动
            if (isForward) {
                boolean isCanScrollToLeft = mView.canScrollHorizontally(-1);
                mIsDragRight = !isCanScrollToLeft;
                return isCanScrollToLeft;
            } else {
                boolean isCanScrollToRight = mView.canScrollHorizontally(1);
                mIsDragLeft = !isCanScrollToRight;
                return isCanScrollToRight;
            }
        } else {    // 垂直滑动
            if (isForward) {
                boolean isCanScrollToTop = mView.canScrollVertically(-1);
                mIsDragBottom = !isCanScrollToTop;
                return isCanScrollToTop;
            } else {
                boolean isCanScrollToBottom = mView.canScrollVertically(1);
                mIsDragTop = !mIsDragBottom;
                return isCanScrollToBottom;
            }
        }
    }

    @Override
    public void onDrag(float offset) {
        if (mIsDragRight) {
            setHeaderMargin(-mHeaderViewSize + (int)offset);
            if (null != mHeaderWrapper && offset != mHeaderOffset) {
                mHeaderWrapper.onPull(offset, offset > mHeaderOffset, offset >= mHeaderViewSize);
                mHeaderOffset = offset;
            }
        } else if (mIsDragLeft) {
            setFooterMargin(-mFooterViewSize + (int)offset);
            if (null != mFooterWrapper && offset != mFooterOffset) {
                mFooterWrapper.onPull(offset, offset > mFooterOffset, offset >= mFooterViewSize);
                mFooterOffset = offset;
            }
        } else if (mIsDragBottom) {
            setHeaderMargin(-mHeaderViewSize + (int)offset);
            if (null != mHeaderWrapper && offset != mHeaderOffset) {
                mHeaderWrapper.onPull(offset, offset > mHeaderOffset, offset >= mHeaderViewSize);
                mHeaderOffset = offset;
            }
        } else if (mIsDragTop) {
            setFooterMargin(-mFooterViewSize + (int)offset);
            if (null != mFooterWrapper && offset != mFooterOffset) {
                mFooterWrapper.onPull(offset, offset > mFooterOffset, offset >= mFooterViewSize);
                mFooterOffset = offset;
            }
        }
    }

    @Override
    public void onRelease(float maxOffset, float offset) {
        boolean isDoRestoreAction = true;
        if (mIsDragRight) {
            if (null != mHeaderWrapper) {
                if (offset < mHeaderViewSize) {
                    mHeaderWrapper.onPullAbort();
                } else  {
                    mHeaderWrapper.onRefreshing();
                    isDoRestoreAction = false;
                }
            }
        } else if (mIsDragLeft) {
            if (null != mFooterWrapper) {
                if (offset < mFooterViewSize) {
                    mFooterWrapper.onPullAbort();
                } else {
                    mFooterWrapper.onRefreshing();
                    isDoRestoreAction = false;
                }
            }
        } else if (mIsDragBottom) {
            if (null != mHeaderWrapper) {
                if (offset < mHeaderViewSize) {
                    mHeaderWrapper.onPullAbort();
                } else  {
                    mHeaderWrapper.onRefreshing();
                    isDoRestoreAction = false;
                }
            }
        } else if (mIsDragTop) {
            if (null != mFooterWrapper ) {
                if (offset < mFooterViewSize) {
                    mFooterWrapper.onPullAbort();
                } else {
                    mFooterWrapper.onRefreshing();
                    isDoRestoreAction = false;
                }
            }
        }
        if (isDoRestoreAction) {
            restore();
        }
        mIsDragRight = mIsDragLeft = mIsDragBottom = mIsDragTop = false;
        mHeaderOffset = mFooterOffset = 0;
    }

    // 初始化
    private void initialize() {
        // 视图
        mViewLayout = new SpringLayout(getContext());
        mViewLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mViewLayout.setListener(this);
        addView(mViewLayout);
        mView = new RecyclerView(getContext());
        mView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mViewLayout.addView(mView);
        // 页眉
        mHeader = new RelativeLayout(getContext());
        addView(mHeader);
        // 页脚
        mFooter = new RelativeLayout(getContext());
        addView(mFooter);
        setHorizontal(mViewLayout.isHorizontal());
    }

    // 设置页眉marign
    private void setHeaderMargin(int margin) {
        if (margin < -mHeaderViewSize) {
            margin = -mHeaderViewSize;
        }
        LayoutParams lp = (LayoutParams)mHeader.getLayoutParams();
        if (mViewLayout.isHorizontal()) {
            lp.leftMargin = margin;
        } else {
            lp.topMargin = margin;
        }
        mHeader.setLayoutParams(lp);
    }

    // 设置页脚marign
    private void setFooterMargin(int margin) {
        if (margin < -mFooterViewSize) {
            margin = -mFooterViewSize;
        }
        LayoutParams lp = (LayoutParams)mFooter.getLayoutParams();
        if (mViewLayout.isHorizontal()) {
            lp.rightMargin = margin;
        } else {
            lp.bottomMargin = margin;
        }
        mFooter.setLayoutParams(lp);
    }

    /**
     * 功  能: 获取视图
     * 参  数: 无
     * 返回值: RecyclerView
     */
    public RecyclerView getView() {
        return mView;
    }

    /**
     * 功  能: 设置水平滑动
     * 参  数: isHorizontal - 水平滑动
     * 返回值: 无
     */
    public void setHorizontal(boolean isHorizontal) {
        mViewLayout.setHorizontal(isHorizontal);
        // 页眉位置
        LayoutParams headerLP = (LayoutParams)mHeader.getLayoutParams();
        if (isHorizontal) {
            headerLP.width = LayoutParams.WRAP_CONTENT;
            headerLP.height = LayoutParams.MATCH_PARENT;
            headerLP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        } else {
            headerLP.width = LayoutParams.MATCH_PARENT;
            headerLP.height = LayoutParams.WRAP_CONTENT;
            headerLP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        }
        mHeader.setLayoutParams(headerLP);
        // 页脚位置
        LayoutParams footerLP = (LayoutParams)mFooter.getLayoutParams();
        if (isHorizontal) {
            footerLP.width = LayoutParams.WRAP_CONTENT;
            footerLP.height = LayoutParams.MATCH_PARENT;
            footerLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        } else {
            footerLP.width = LayoutParams.MATCH_PARENT;
            footerLP.height = LayoutParams.WRAP_CONTENT;
            footerLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        mFooter.setLayoutParams(footerLP);
    }

    /**
     * 功  能: 设置页眉包装器
     * 参  数: wrapper - 页眉包装器
     * 返回值: 无
     */
    public void setHeaderWrapper(Wrapper wrapper) {
        if (null != mHeaderWrapper) {
            throw new AssertionError("exist header wrapper");
        }
        mHeaderWrapper = wrapper;
        mHeaderView = wrapper.createView(getContext(), mHeader);
        if (null != mHeaderView) {
            mHeader.addView(mHeaderView);
        }
    }

    /**
     * 功  能: 设置页脚包装器
     * 参  数: wrapper - 页脚包装器
     * 返回值: 无
     */
    public void setFooterWrapper(Wrapper wrapper) {
        if (null != mFooterWrapper) {
            throw new AssertionError("exist footer wrapper");
        }
        mFooterWrapper = wrapper;
        mFooterView = wrapper.createView(getContext(), mFooter);
        if (null != mFooterView) {
            mFooter.addView(mFooterView);
        }
    }

    /**
     * 功  能: 复位滑动
     * 参  数: 无
     * 返回值: 无
     */
    public void restore() {
        setHeaderMargin(-mHeaderViewSize);
        setFooterMargin(-mFooterViewSize);
        mViewLayout.actionBack();
    }

    /**
     * Brief:   Header/Footer Wrapper
     */
    public interface Wrapper {
        /**
         * 功  能: 创建视图
         * 参  数: context - 上下文
         *         parent - 父节点
         * 返回值: View
         */
        View createView(Context context, RelativeLayout parent);

        /**
         * 功  能: 在滑动中
         * 参  数: offset - 当前偏移
         *         isForward - 是否向前,例:当下拉刷新时,如果当前下拉距离>上次下拉距离,则为向前,否则为退后
         *         isReady - 是否放开就可以刷新
         * 返回值: 无
         */
        void onPull(float offset, boolean isForward, boolean isReady);

        /**
         * 功  能: 滑动终止
         * 参  数: 无
         * 返回值: 无
         */
        void onPullAbort();

        /**
         * 功  能: 刷新中
         * 参  数: 无
         * 返回值: 无
         */
        void onRefreshing();
    }
}
