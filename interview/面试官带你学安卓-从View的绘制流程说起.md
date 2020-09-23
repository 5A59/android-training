## 面试官带你学安卓 - 从 View 的绘制流程说起
### 一、写面试官系列的原因
说来惭愧，已经有很长时间没有更新技术文章了。   

现在突然更新的原因是，最近面试了一些同学，对于一些知识点，不少候选人同学其实掌握的不够扎实，而在对于一些知识点逐渐深入的过程中，发现我自己也有些遗忘了，所以想重新梳理一下，把过程记录下来，给大家一些参考，如果能有帮助，那最好不过了。    

这一篇就先从 View 开始。    

对于安卓开发来说，我想除了 Activity 以外，就是 View 接触的最多了。这篇文章就以面试官的角度来讲讲 View 的一些知识点，看看问题是如何一层层的深入下去的。     

### 二、View 题目层次
我们以最常见的两个面试题目（View 的绘制流程 和 View 的事件分发）开始，逐层深入去看一下。     

先上 View 的绘制流程。    

View 的绘制流程是 measure -> layout -> draw，这个大家都熟悉。   

不过由此引申的知识点还是有不少的：

1. 首次 View 的绘制流程是在什么时候触发的？
2. ViewRootImpl 创建的时机？
3. ViewRootImpl 和 DecorView 的关系是什么？
4. DecorView 的布局是什么样的？
5. DecorView 的创建时机？
6. setContentView 的流程
7. LayoutInflate 的流程
8. Activity、PhoneWindow、DecorView、ViewRootImpl 的关系？
9. PhoneWindow 的创建时机？
10. 如何触发重新绘制？
11. requestLayout 和 invalidate 的流程
12. requestLayout 和 invalidate 的区别

上面这些就是我想到的由 View 绘制流程引申的一系列问题，其实如果细想，还会有很多，这里就作为个引子。下面看看问题的详解（以下代码分析基于 SDK 28）。   

如果上面的问题读者朋友们都能回答上来，也就没有必要往下看了～    

### 三、题目详解
#### 1. 首次 View 的绘制流程是在什么时候触发的？
既然开始说到了 View 的绘制流程，那整个流程是什么时候触发的呢？

答案在 ActivityThread.handleResumeActivity 里触发的。  

``` java
    public void handleResumeActivity(IBinder token, boolean finalStateRequest, boolean isForward,
            String reason) {

        // ...
        if (r.window == null && !a.mFinished && willBeVisible) {
            r.window = r.activity.getWindow();
            View decor = r.window.getDecorView();
            decor.setVisibility(View.INVISIBLE);
            ViewManager wm = a.getWindowManager();
            WindowManager.LayoutParams l = r.window.getAttributes();
            a.mDecor = decor;
            // ...
            if (a.mVisibleFromClient) {
                if (!a.mWindowAdded) {
                    a.mWindowAdded = true;
                    wm.addView(decor, l);
                } else {
                  // ...
                }
            }

            // If the window has already been added, but during resume
            // we started another activity, then don't yet make the
            // window visible.
        } else if (!willBeVisible) {
            if (localLOGV) Slog.v(TAG, "Launch " + r + " mStartedActivity set");
            r.hideForNow = true;
        }
        // ...

    }
```

ActivityThread.handleResumeActivity 里会调用 wm.addView 来添加 DecorView，wm 是 WindowManagerImpl   

``` java
// WindowManagerImpl
    public void addView(@NonNull View view, @NonNull ViewGroup.LayoutParams params) {
        applyDefaultToken(params);
        mGlobal.addView(view, params, mContext.getDisplay(), mParentWindow);
    }
// WindowManagerGlobal
    public void addView(View view, ViewGroup.LayoutParams params,
            Display display, Window parentWindow) {
        // 这里的 view 就是 DecorView
        // ...
        ViewRootImpl root;
        View panelParentView = null;

        synchronized (mLock) {
            // ...
            root = new ViewRootImpl(view.getContext(), display);

            view.setLayoutParams(wparams);

            mViews.add(view);
            mRoots.add(root);
            mParams.add(wparams);

            // do this last because it fires off messages to start doing things
            try {
                root.setView(view, wparams, panelParentView);
            } catch (RuntimeException e) {
            }
        }
    }
// ViewRootImpl.setView
    public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
      requestLayout();
    }
```

最终通过 WindowManagerImpl.addView -> WindowManagerGlobal.addView -> ViewRootImpl.setView -> ViewRootImpl.requestLayout 就触发了第一次 View 的绘制。

#### 2. ViewRootImpl 创建的时机？
从上面流程里可以看到，ViewRootImpl 也是在 ActivityThread.handleResumeActivity 里创建的。

#### 3. ViewRootImpl 和 DecorView 的关系是什么？
``` java
// ViewRootImpl.setView
    public void setView(View view, WindowManager.LayoutParams attrs, View panelParentView) {
      requestLayout();
      // ...
      // 这里的 view 是 DecorView
      view.assignParent(this);
    }
```
接着上面的代码看，在 ViewRootImpl.setView 里，通过 DecorView.assignParent 把 ViewRootImpl 设置为 DecorView 的 parent。

所以 ViewRootImpl 和 DecorView 的关系就是 ViewRootImpl 是 DecorView 的 parent。   

因为 DecorView 是我们布局的顶层，现在我们就知道层层调用 requestLayout 等方法是怎么调用到 ViewRootImpl 里的了。

#### 4. DecorView 的布局是什么样的？
对于 Activity 的层级，大家应该都看过一张图的描述，Activity -> PhoneWindow -> DecorView -> [title_bar, content]，其中 DecorView 里包括了 title_bar 和 content 两个 View，不过这个是默认的布局，实际上根据不同的主题样式，DecorView 对应有不同的布局。   

图中所包含的 title_bar 和 content 对应的是 R.layout.screen_simple 布局。  

那么这么多布局，是在什么时候设置的呢？

是在 PhoneWindow.installDecor -> generateLayout 中设置的。
``` java
// PhoneWindow
    private void installDecor() {
        mForceDecorInstall = false;
        if (mDecor == null) {
            // 生成 DecorView
            mDecor = generateDecor(-1);
            mDecor.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            mDecor.setIsRootNamespace(true);
            if (!mInvalidatePanelMenuPosted && mInvalidatePanelMenuFeatures != 0) {
                mDecor.postOnAnimation(mInvalidatePanelMenuRunnable);
            }
        } else {
            mDecor.setWindow(this);
        }
        if (mContentParent == null) {
            mContentParent = generateLayout(mDecor); // 生成 DecorView 子View
        }
    }

    protected ViewGroup generateLayout(DecorView decor) {
        // 根据不同的 window feature 给 DecorView 设置不同的布局
        int layoutResource;
        int features = getLocalFeatures();
        if ((features & (1 << FEATURE_SWIPE_TO_DISMISS)) != 0) {
            layoutResource = R.layout.screen_swipe_dismiss;
            setCloseOnSwipeEnabled(true);
        } else if ((features & ((1 << FEATURE_LEFT_ICON) | (1 << FEATURE_RIGHT_ICON))) != 0) {
            if (mIsFloating) {
                TypedValue res = new TypedValue();
                getContext().getTheme().resolveAttribute(
                        R.attr.dialogTitleIconsDecorLayout, res, true);
                layoutResource = res.resourceId;
            } else {
                layoutResource = R.layout.screen_title_icons;
            }
            removeFeature(FEATURE_ACTION_BAR);
        } else if ((features & ((1 << FEATURE_PROGRESS) | (1 << FEATURE_INDETERMINATE_PROGRESS))) != 0
                && (features & (1 << FEATURE_ACTION_BAR)) == 0) {
            layoutResource = R.layout.screen_progress;
        } else if ((features & (1 << FEATURE_CUSTOM_TITLE)) != 0) {
            if (mIsFloating) {
                TypedValue res = new TypedValue();
                getContext().getTheme().resolveAttribute(
                        R.attr.dialogCustomTitleDecorLayout, res, true);
                layoutResource = res.resourceId;
            } else {
                layoutResource = R.layout.screen_custom_title;
            }
            removeFeature(FEATURE_ACTION_BAR);
        } else if ((features & (1 << FEATURE_NO_TITLE)) == 0) {
            if (mIsFloating) {
                TypedValue res = new TypedValue();
                getContext().getTheme().resolveAttribute(
                        R.attr.dialogTitleDecorLayout, res, true);
                layoutResource = res.resourceId;
            } else if ((features & (1 << FEATURE_ACTION_BAR)) != 0) {
                layoutResource = a.getResourceId(
                        R.styleable.Window_windowActionBarFullscreenDecorLayout,
                        R.layout.screen_action_bar);
            } else {
                layoutResource = R.layout.screen_title;
            }
        } else if ((features & (1 << FEATURE_ACTION_MODE_OVERLAY)) != 0) {
            layoutResource = R.layout.screen_simple_overlay_action_mode;
        } else {
            // 默认布局
            layoutResource = R.layout.screen_simple;
        }

        mDecor.startChanging();
        mDecor.onResourcesLoaded(mLayoutInflater, layoutResource);
    }

// DecorView
    void onResourcesLoaded(LayoutInflater inflater, int layoutResource) {
        // 根据 上一步选择的 layout 生成 View
        final View root = inflater.inflate(layoutResource, null);
        if (mDecorCaptionView != null) {
            if (mDecorCaptionView.getParent() == null) {
                addView(mDecorCaptionView,
                        new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
            }
            mDecorCaptionView.addView(root,
                    new ViewGroup.MarginLayoutParams(MATCH_PARENT, MATCH_PARENT));
        } else {
            // 添加到 DecorView 里
            addView(root, 0, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        }
        mContentRoot = (ViewGroup) root;
        initializeElevation();
    }
```

#### 5. DecorView 的创建时机？
上面说 DecorView 布局的时候，其实我们也看到了，在 PhoneWindow.installDecor -> generateDecor 其实就是创建 DecorView。   

那 installDecor 是什么时候调用的呢？   

调用链是 Activity.setContentView -> PhoneWindow.setContentView -> installDecor

说到这里那就继续会想到，Activity.setContentView 的流程是什么呢？

#### 6. setContentView 的流程
setContentView 流程比较简单，会调用 PhoneWindow.setContentView。   

其中做的事情是两个：

1. 创建 DecorView
2. 根据 layoutResId 创建 View 并添加到 DecorView 中

``` java
    @Override
    public void setContentView(int layoutResID) {
        if (mContentParent == null) {
          // 创建 DecorView
            installDecor();
        } else if (!hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            mContentParent.removeAllViews();
        }

        if (hasFeature(FEATURE_CONTENT_TRANSITIONS)) {
            final Scene newScene = Scene.getSceneForLayout(mContentParent, layoutResID,
                    getContext());
            transitionTo(newScene);
        } else {
          // 根据 layoutResId 创建 ContentView
            mLayoutInflater.inflate(layoutResID, mContentParent);
        }
        mContentParent.requestApplyInsets();
        final Callback cb = getCallback();
        if (cb != null && !isDestroyed()) {
            cb.onContentChanged();
        }
        mContentParentExplicitlySet = true;
    }
```

#### 7. LayoutInflate 的流程
既然上一步用到了 LayoutInflate.inflate，那使用 LayoutInflate.inflate 加载一个布局的流程是什么样的呢？   

``` java
    public View inflate(@LayoutRes int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        final Resources res = getContext().getResources();
        // 通过 resourceId 获取 xml 布局内容
        final XmlResourceParser parser = res.getLayout(resource);
        try {
            return inflate(parser, root, attachToRoot);
        } finally {
            parser.close();
        }
    }

    public View inflate(XmlPullParser parser, @Nullable ViewGroup root, boolean attachToRoot) {
        synchronized (mConstructorArgs) {
            // ...
            View result = root;
            try {
                // Look for the root node.
                int type;
                // 找到 xml start 或者 xml end
                while ((type = parser.next()) != XmlPullParser.START_TAG &&
                        type != XmlPullParser.END_DOCUMENT) {
                    // Empty
                }

                if (type != XmlPullParser.START_TAG) {
                    throw new InflateException(parser.getPositionDescription()
                            + ": No start tag found!");
                }

                final String name = parser.getName();

                // 处理 merge 标签
                if (TAG_MERGE.equals(name)) {
                    if (root == null || !attachToRoot) {
                        throw new InflateException("<merge /> can be used only with a valid "
                                + "ViewGroup root and attachToRoot=true");
                    }
                    // merge 标签传入的 parent 是 rootView
                    rInflate(parser, root, inflaterContext, attrs, false);
                } else {
                    // 通过 tag 创建 View
                    final View temp = createViewFromTag(root, name, inflaterContext, attrs);

                    ViewGroup.LayoutParams params = null;

                    if (root != null) {
                        // 使用 rootView 默认的 LayoutParams
                        params = root.generateLayoutParams(attrs);
                        if (!attachToRoot) {
                            temp.setLayoutParams(params);
                        }
                    }

                    // 创建子 View
                    rInflateChildren(parser, temp, attrs, true);

                    if (root != null && attachToRoot) {
                        // 添加到 rootView
                        root.addView(temp, params);
                    }

                    // Decide whether to return the root that was passed in or the
                    // top view found in xml.
                    if (root == null || !attachToRoot) {
                        result = temp;
                    }
                }

            } catch (XmlPullParserException e) {
            } finally {
            }
            return result;
        }
    }

    final void rInflateChildren(XmlPullParser parser, View parent, AttributeSet attrs,
            boolean finishInflate) throws XmlPullParserException, IOException {
        rInflate(parser, parent, parent.getContext(), attrs, finishInflate);
    }

    void rInflate(XmlPullParser parser, View parent, Context context,
            AttributeSet attrs, boolean finishInflate) throws XmlPullParserException, IOException {

        while (((type = parser.next()) != XmlPullParser.END_TAG ||
                parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            final String name = parser.getName();

            if (TAG_REQUEST_FOCUS.equals(name)) {
                pendingRequestFocus = true;
                consumeChildElements(parser);
            } else if (TAG_TAG.equals(name)) {
                parseViewTag(parser, parent, attrs);
            } else if (TAG_INCLUDE.equals(name)) {
                // 处理 include 标签
                if (parser.getDepth() == 0) {
                    throw new InflateException("<include /> cannot be the root element");
                }
                parseInclude(parser, context, parent, attrs);
            } else if (TAG_MERGE.equals(name)) {
                throw new InflateException("<merge /> must be the root element");
            } else {
                // 通过 xml 标签生成 View
                final View view = createViewFromTag(parent, name, context, attrs);
                final ViewGroup viewGroup = (ViewGroup) parent;
                final ViewGroup.LayoutParams params = viewGroup.generateLayoutParams(attrs);
                rInflateChildren(parser, view, attrs, true);
                viewGroup.addView(view, params);
            }
        }

    }

```

上面的流程可以看到，LayoutInflate.inflate 最终是调用 createViewFromTag 从 xml 生成 View 的，其实这里才是关键。  

``` java
    View createViewFromTag(View parent, String name, Context context, AttributeSet attrs,
            boolean ignoreThemeAttr) {
        /** 如果是 view 标签的话，就取其 class 属性作为 name
        * 比如 
        * <view class="LinearLayout"/>
        * 最终生成的会是一个 LinearLayout
        * 是不是又学会了一种 view 的写法 ^_^
        */
        if (name.equals("view")) {
            name = attrs.getAttributeValue(null, "class");
        }

        // 处理 blink 标签
        if (name.equals(TAG_1995)) {
            return new BlinkLayout(context, attrs);
        }

        try {
          // 通过 mFactory2、mFactory、mPrivateFactory 创建 View
            View view;
            if (mFactory2 != null) {
                view = mFactory2.onCreateView(parent, name, context, attrs);
            } else if (mFactory != null) {
                view = mFactory.onCreateView(name, context, attrs);
            } else {
                view = null;
            }

            if (view == null && mPrivateFactory != null) {
                view = mPrivateFactory.onCreateView(parent, name, context, attrs);
            }

            // 没有设置 Factory，走默认的创建 View 的流程
            if (view == null) {
                final Object lastContext = mConstructorArgs[0];
                mConstructorArgs[0] = context;
                try {
                    if (-1 == name.indexOf('.')) {
                        view = onCreateView(parent, name, attrs);
                    } else {
                        view = createView(name, null, attrs);
                    }
                } finally {
                    mConstructorArgs[0] = lastContext;
                }
            }

            return view;
        } catch (InflateException e) {
        }
    }
```

这里我们需要了解一下，mFactory、mFactory2、mPrivateFactory 都是什么？   

``` java
    private Factory mFactory;
    private Factory2 mFactory2;
    private Factory2 mPrivateFactory;

    public interface Factory {
        public View onCreateView(String name, Context context, AttributeSet attrs);
    }

    public interface Factory2 extends Factory {
        public View onCreateView(View parent, String name, Context context, AttributeSet attrs);
    }
```
mFactory、mFactory2、mPrivateFactory 分别对应 Factory 和 Factory2 方法，对应的是两个 onCreateView 方法，Factory.onCreateView 没有传入 parent 参数，Factory2.onCreateView 传入了 parent 参数。

而 mFactory 和 mFactory2 我们是可以设置的，当然不能重复设置，重复设置会抛出异常。  

如果已经有 mFactory 的值，则生成一个 FactoryMerger，这个也是继承了 Factory2，用来控制一下调用顺序。  

具体代码如下   
``` java
    public void setFactory(Factory factory) {
        if (mFactorySet) {
            throw new IllegalStateException("A factory has already been set on this LayoutInflater");
        }
        if (factory == null) {
            throw new NullPointerException("Given factory can not be null");
        }
        mFactorySet = true;
        if (mFactory == null) {
            mFactory = factory;
        } else {
            mFactory = new FactoryMerger(factory, null, mFactory, mFactory2);
        }
    }

    public void setFactory2(Factory2 factory) {
        if (mFactorySet) {
            throw new IllegalStateException("A factory has already been set on this LayoutInflater");
        }
        if (factory == null) {
            throw new NullPointerException("Given factory can not be null");
        }
        mFactorySet = true;
        if (mFactory == null) {
            mFactory = mFactory2 = factory;
        } else {
            mFactory = mFactory2 = new FactoryMerger(factory, factory, mFactory, mFactory2);
        }
    }

    private static class FactoryMerger implements Factory2 {
        private final Factory mF1, mF2;
        private final Factory2 mF12, mF22;

        FactoryMerger(Factory f1, Factory2 f12, Factory f2, Factory2 f22) {
            mF1 = f1;
            mF2 = f2;
            mF12 = f12;
            mF22 = f22;
        }

        public View onCreateView(String name, Context context, AttributeSet attrs) {
            View v = mF1.onCreateView(name, context, attrs);
            if (v != null) return v;
            return mF2.onCreateView(name, context, attrs);
        }

        public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
            View v = mF12 != null ? mF12.onCreateView(parent, name, context, attrs)
                    : mF1.onCreateView(name, context, attrs);
            if (v != null) return v;
            return mF22 != null ? mF22.onCreateView(parent, name, context, attrs)
                    : mF2.onCreateView(name, context, attrs);
        }
    }
```
然后我们再看 mPrivateFactory，看名称就知道是系统的隐藏方法。  

调用时机是在 Activity.attach 中，Activity 其实是实现了 Factory2 的 onCreateView 方法，其中对 fragment 做了处理，如果是 fragment 标签，就调用 fragment 的 onCreateView，这里就不详细往下面看了，如果是非 fragment 的标签，就返回 null，走默认的创建 View 的方法。

``` java
    /**
     * @hide for use by framework
     */
    public void setPrivateFactory(Factory2 factory) {
        if (mPrivateFactory == null) {
            mPrivateFactory = factory;
        } else {
            mPrivateFactory = new FactoryMerger(factory, factory, mPrivateFactory, mPrivateFactory);
        }
    }

// Activity
    final void attach(...)
        mWindow.getLayoutInflater().setPrivateFactory(this);
    }

    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }

    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        if (!"fragment".equals(name)) {
            return onCreateView(name, context, attrs);
        }

        return mFragments.onCreateView(parent, name, context, attrs);
    }
```

所以上面的 Factory 和 Factory2，是系统留给我们的 hook View 创建流程的接口。   

如果都没有设置，那就走到默认的创建 View 的方法。

默认创建 View 的方法比较简单，就是反射调用 View 的构造函数，然后做一个缓存，然后创建 View。   

具体代码如下
``` java
// LayoutInflate
    View createViewFromTag(View parent, String name, Context context, AttributeSet attrs,
        boolean ignoreThemeAttr) {
          // 前面的 mFactory、mFactory2、mPrivateFactory 都没有去创建 View
        if (view == null) {
            final Object lastContext = mConstructorArgs[0];
            mConstructorArgs[0] = context;
            try {
                if (-1 == name.indexOf('.')) {
                  // 如果名称里没有 “.”，也就是系统的 View，需要添加 android.view. 前缀，比如 <LinearLayout />，最终去创建的名称是 android.view.LinearLayout
                    view = onCreateView(parent, name, attrs);
                } else {
                  // 如果是自定义 View，则直接去创建
                    view = createView(name, null, attrs);
                }
            } finally {
                mConstructorArgs[0] = lastContext;
            }
        }
        // ...
    }

    protected View onCreateView(String name, AttributeSet attrs)
            throws ClassNotFoundException {
        return createView(name, "android.view.", attrs);
    }

    public final View createView(String name, String prefix, AttributeSet attrs)
            throws ClassNotFoundException, InflateException {
        Constructor<? extends View> constructor = sConstructorMap.get(name);
        if (constructor != null && !verifyClassLoader(constructor)) {
            constructor = null;
            sConstructorMap.remove(name);
        }
        Class<? extends View> clazz = null;

        try {
            if (constructor == null) {
              // 加载对应的类
                clazz = mContext.getClassLoader().loadClass(
                        prefix != null ? (prefix + name) : name).asSubclass(View.class);
                // 反射获取构造函数
                constructor = clazz.getConstructor(mConstructorSignature);
                constructor.setAccessible(true);
                // 做个缓存，下次直接使用，提高效率
                sConstructorMap.put(name, constructor);
            } else {
            }

            Object lastContext = mConstructorArgs[0];
            if (mConstructorArgs[0] == null) {
                // Fill in the context if not already within inflation.
                mConstructorArgs[0] = mContext;
            }
            Object[] args = mConstructorArgs;
            args[1] = attrs;

            // 调用构造函数创建 View
            final View view = constructor.newInstance(args);
            if (view instanceof ViewStub) {
                // 处理 ViewStub
                final ViewStub viewStub = (ViewStub) view;
                viewStub.setLayoutInflater(cloneInContext((Context) args[0]));
            }
            mConstructorArgs[0] = lastContext;
            return view;
        } catch (NoSuchMethodException e) {
        }
    }
```
所以上面就是 LayoutInflate.inflate 的整个流程。   

#### 8. Activity、PhoneWindow、DecorView、ViewRootImpl 的关系？
其实上面的问题中，我们经常会说到 PhoneWindow 这个角色，PhoneWindow 其实是 Window 的唯一子类，是 Activity 和 View 交互系统的中间层，而 DecorView 是整个 View 层级的最顶层，ViewRootImpl 是 DecorView 的 parent，但是他并不是一个真正的 View，只是继承了 ViewParent 接口，用来掌管 View 的各种事件，包括 requestLayout、invalidate、dispatchInputEvent 等等。   

#### 9. PhoneWindow 的创建时机？  
既然上面又提到了 PhoneWindow，那么 PhoneWindow 是什么时候创建的呢？是在 Activity.attach 里创建的，而 Activity.attach 又是在 ActivityThread.performLaunchActivity 里创建的。  

这里就又能引申出 Activity 的启动流程，这里就先不讲了。   

#### 10. 如何触发重新绘制？
既然上面说到 View 的绘制流程，那我们怎么触发 View 的重新绘制呢？  

就是调用 requestLayout 和 invalidate。   

#### 11. requestLayout 和 invalidate 的流程
**requestLayout 流程**    
``` java
// View
    public void requestLayout() {
        if (mMeasureCache != null) mMeasureCache.clear();

        if (mAttachInfo != null && mAttachInfo.mViewRequestingLayout == null) {
            ViewRootImpl viewRoot = getViewRootImpl();
            if (viewRoot != null && viewRoot.isInLayout()) {
                if (!viewRoot.requestLayoutDuringLayout(this)) { 
                  // 如果当前在 layout 流程中，并且是在处理 requestLayout，那么就直接返回，这个时候需要注意，mPrivateFlags 并没有设置 FORCE_LAYOUT
                  // 这个时候 reqeustLayout 会在下一个 frame 里执行
                    return;
                }
            }
            mAttachInfo.mViewRequestingLayout = this;
        }

        // 如果当前在 layout 流程中，但是没有处理 requestLayout，那么就继续后面的流程，这个时候 mPrivateFlags 是设置为 FORCE_LAYOUT
        // 这个时候 requestLayout 会在下一次 layout 过程中进行执行

        // 设置 FORCE_LAYOUT 和 INVALIDETED flag
        mPrivateFlags |= PFLAG_FORCE_LAYOUT;
        mPrivateFlags |= PFLAG_INVALIDATED;

        if (mParent != null && !mParent.isLayoutRequested()) {
            // 层层调用 parent 的 requestLayout
            mParent.requestLayout();
        }
        if (mAttachInfo != null && mAttachInfo.mViewRequestingLayout == this) {
            mAttachInfo.mViewRequestingLayout = null;
        }
    }
```

从上面代码可以看到，会一层层调用 parent 的 requestLayout，而上面的问题中我们也分析到了，DecorView 是整个 View 层级的最顶层，ViewRootImpl 又是 DecorView 的 parent，所以最终调用到 ViewRootImpl 的 requestLayout。

``` java
// ViewRootImpl
    public void requestLayout() {
        if (!mHandlingLayoutInLayoutRequest) {
            checkThread();
            mLayoutRequested = true;
            scheduleTraversals();
        }
    }
```
ViewRootImpl.requestLayout 调用 scheduleTraversals -> doTraversal -> performTraversals 开启绘制流程。  

其实这里又涉及到了 Choreographer 的一些流程，这里也暂时不展开讲了。

在 performTraversals 里，就是熟悉的 performMeasure -> performLayout -> performDraw 三个流程了。   

先看 performMeasure，最终调用的是 View.measure   
``` java
// View
    public final void measure(int widthMeasureSpec, int heightMeasureSpec) {
      // 这里就是 requestLayout 时设置的 flag，如果执行了 requestLayout，这里 forceLayout 一定是 true
      final boolean forceLayout = (mPrivateFlags & PFLAG_FORCE_LAYOUT) == PFLAG_FORCE_LAYOUT;
      // needsLayout 是 measureSpec 和 oldMeasureSpec 不相符的时候会为 true
      if (forceLayout || needsLayout) {
        onMeasure(widthMeasureSpec, heightMeasureSpec);
      }
      // 设置 LAYOUT_REQUIRED flag，在 layout 中会用到
      mPrivateFlags |= PFLAG_LAYOUT_REQUIRED;
    }
```

再看 performLayout

``` java
    private void performLayout(WindowManager.LayoutParams lp, int desiredWindowWidth,
            int desiredWindowHeight) {
        mLayoutRequested = false;
        mScrollMayChange = true;
        // 表明在 layout 流程中
        mInLayout = true;
        final View host = mView;
        try {
            // 先执行 layout
            host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());

            mInLayout = false;
            // 这里处理在上一次 layout 过程中，调用了 requestLayout 的 View
            int numViewsRequestingLayout = mLayoutRequesters.size();
            if (numViewsRequestingLayout > 0) {
                // requestLayout() was called during layout.
                // If no layout-request flags are set on the requesting views, there is no problem.
                // If some requests are still pending, then we need to clear those flags and do
                // a full request/measure/layout pass to handle this situation.
                // 获取有效的需要 layout 的 View，此时获取的是 mPrivateFlags == PFLAG_FORCE_LAYOUT 的 View，也就是在 View.requestLayout 里设置了 PFLAG_FORCE_LAYOUT 的 View
                ArrayList<View> validLayoutRequesters = getValidLayoutRequesters(mLayoutRequesters,
                        false);
                if (validLayoutRequesters != null) {
                    // Set this flag to indicate that any further requests are happening during
                    // the second pass, which may result in posting those requests to the next
                    // frame instead
                    // 表明当前在处理 requestLayout 
                    mHandlingLayoutInLayoutRequest = true;

                    // Process fresh layout requests, then measure and layout
                    int numValidRequests = validLayoutRequesters.size();
                    for (int i = 0; i < numValidRequests; ++i) {
                        final View view = validLayoutRequesters.get(i);
                        view.requestLayout();
                    }
                    // 执行 measure
                    measureHierarchy(host, lp, mView.getContext().getResources(),
                            desiredWindowWidth, desiredWindowHeight);
                    mInLayout = true;
                    // 执行 Layout
                    host.layout(0, 0, host.getMeasuredWidth(), host.getMeasuredHeight());

                    mHandlingLayoutInLayoutRequest = false;

                    // Check the valid requests again, this time without checking/clearing the
                    // layout flags, since requests happening during the second pass get noop'd
                    // 获取 mPrivateFlags != PFLAG_FORCE_LAYOUT 的 View，也就是在 View.requestLayout 里没有设置 PFLAG_FORCE_LAYOUT 的 View
                    validLayoutRequesters = getValidLayoutRequesters(mLayoutRequesters, true);
                    if (validLayoutRequesters != null) {
                        final ArrayList<View> finalRequesters = validLayoutRequesters;
                        // 在下一次 frame 里再执行一次 requestLayout
                        // 下一次 performTraversals 里会执行 getRunQueue().executeActions(mAttachInfo.mHandler);
                        getRunQueue().post(new Runnable() {
                            @Override
                            public void run() {
                                int numValidRequests = finalRequesters.size();
                                for (int i = 0; i < numValidRequests; ++i) {
                                    final View view = finalRequesters.get(i);
                                    view.requestLayout();
                                }
                            }
                        });
                    }
                }

            }
        } finally {
        }
        mInLayout = false;
    }
```

上面 performLayout 里一共执行了三件事：

1. 执行 View.layout
2. 执行调用过 requestLayout 的 View 的 measure 和 layout
3. 将还没有执行的 requestLayout 加到队列中，下一次 frame 中进行执行

然后看 View.layout 的流程：
``` java
    public void layout(int l, int t, int r, int b) {
        if ((mPrivateFlags3 & PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT) != 0) {
            onMeasure(mOldWidthMeasureSpec, mOldHeightMeasureSpec);
            mPrivateFlags3 &= ~PFLAG3_MEASURE_NEEDED_BEFORE_LAYOUT;
        }

        int oldL = mLeft;
        int oldT = mTop;
        int oldB = mBottom;
        int oldR = mRight;

        // 判断是否位置有变化
        boolean changed = isLayoutModeOptical(mParent) ?
                setOpticalFrame(l, t, r, b) : setFrame(l, t, r, b);

        // 如果位置有变化，或者设置了 PFLAG_LAYOUT_REQUIRED，PFLAG_LAYOUT_REQUIRED 是在 View.measure 结束以后设置的
        if (changed || (mPrivateFlags & PFLAG_LAYOUT_REQUIRED) == PFLAG_LAYOUT_REQUIRED) {
            onLayout(changed, l, t, r, b);
            // ...
            // 取消 flag
            mPrivateFlags &= ~PFLAG_LAYOUT_REQUIRED;
            // ...
        }
        // 取消 flag
        mPrivateFlags &= ~PFLAG_FORCE_LAYOUT;
        mPrivateFlags3 |= PFLAG3_IS_LAID_OUT;
        // ...
    }
```

最后就是 ViewRootImpl.performDraw -> draw 了。   

``` java
// ViewRootImpl
    private boolean draw(boolean fullRedrawNeeded) {
      // 有 dirty 区域会进行重绘
      if (!dirty.isEmpty() || mIsAnimating || accessibilityFocusDirty) {
        if (fullRedrawNeeded) {
            mAttachInfo.mIgnoreDirtyState = true;
            // 如果需要全部重绘，把 dirty 区域设置成 DecorView 的区域
            dirty.set(0, 0, (int) (mWidth * appScale + 0.5f), (int) (mHeight * appScale + 0.5f));
        }
        // drawSoftware 调用了 DecorView.draw
        if (!drawSoftware(surface, mAttachInfo, xOffset, yOffset,
                scalingRequired, dirty, surfaceInsets)) {
            return false;
        }
      }
    }

// View
    public void draw(Canvas canvas) {
        final int privateFlags = mPrivateFlags;
        // flag 是 PFLAG_DIRTY_OPAQUE 则需要绘制
        final boolean dirtyOpaque = (privateFlags & PFLAG_DIRTY_MASK) == PFLAG_DIRTY_OPAQUE &&
                (mAttachInfo == null || !mAttachInfo.mIgnoreDirtyState);
        mPrivateFlags = (privateFlags & ~PFLAG_DIRTY_MASK) | PFLAG_DRAWN;
        if (!dirtyOpaque) {
            drawBackground(canvas);
        }
        if (!dirtyOpaque) onDraw(canvas);
        // 绘制 Child
        dispatchDraw(canvas);
        // foreground 不管 dirtyOpaque 标志，每次都会绘制
        onDrawForeground(canvas);
    }
```
在 View 的绘制过程中，我们可以看到，**只有 flag 被设置为 PFLAG_DIRTY_OPAQUE 才会进行绘制（这里划重点）**。   

这也就是大家经常说的 requestLayout 不会引发 draw。   

**invalidate 流程**    
invalidate -> invalidateInternal 的主要流程就是在设置 mPrivateFlags 
``` java
    void invalidateInternal(int l, int t, int r, int b, boolean invalidateCache,
            boolean fullInvalidate) {
        // ...
        if ((mPrivateFlags & (PFLAG_DRAWN | PFLAG_HAS_BOUNDS)) == (PFLAG_DRAWN | PFLAG_HAS_BOUNDS)
                || (invalidateCache && (mPrivateFlags & PFLAG_DRAWING_CACHE_VALID) == PFLAG_DRAWING_CACHE_VALID)
                || (mPrivateFlags & PFLAG_INVALIDATED) != PFLAG_INVALIDATED
                || (fullInvalidate && isOpaque() != mLastIsOpaque)) {
            if (fullInvalidate) {
                mLastIsOpaque = isOpaque();
                mPrivateFlags &= ~PFLAG_DRAWN;
            }

            // 设置 dirty flag
            mPrivateFlags |= PFLAG_DIRTY;

            if (invalidateCache) {
                mPrivateFlags |= PFLAG_INVALIDATED;
                mPrivateFlags &= ~PFLAG_DRAWING_CACHE_VALID;
            }

            if (p != null && ai != null && l < r && t < b) {
                final Rect damage = ai.mTmpInvalRect;
                damage.set(l, t, r, b);
                p.invalidateChild(this, damage);
            }
            // ...
        }
    }
```

invalidate 会调用 parent.invalidateChild

``` java
    public final void invalidateChild(View child, final Rect dirty) {
                  final boolean drawAnimation = (child.mPrivateFlags & PFLAG_DRAW_ANIMATION) != 0;
                  // child 不透明的条件是没有动画且 child 本身是不透明的
            final boolean isOpaque = child.isOpaque() && !drawAnimation &&
                    child.getAnimation() == null && childMatrix.isIdentity();
            // 不透明的话使用的是 PFLAG_DIRTY_OPAQUE flag
            int opaqueFlag = isOpaque ? PFLAG_DIRTY_OPAQUE : PFLAG_DIRTY;
            do {
                View view = null;
                if (parent instanceof View) {
                    view = (View) parent;
                }
                if ((view.mPrivateFlags & PFLAG_DIRTY_MASK) != PFLAG_DIRTY) {
                    // 设置 flag 为 PFLAG_DIRTY_OPAQUE
                    view.mPrivateFlags = (view.mPrivateFlags & ~PFLAG_DIRTY_MASK) | opaqueFlag;
                }
                // 计算 parent 的 dirty 区域
                parent = parent.invalidateChildInParent(location, dirty);
            } while (parent != null);
    }
```

上面的 while 循环里，会层层计算 parent 的 dirty 区域，最终会调用到 ViewRootImpl.invalidateChildInParent -> invalidateRectOnScreen

``` java
    private void invalidateRectOnScreen(Rect dirty) {
        final Rect localDirty = mDirty;
        if (!localDirty.isEmpty() && !localDirty.contains(dirty)) {
            mAttachInfo.mSetIgnoreDirtyState = true;
            mAttachInfo.mIgnoreDirtyState = true;
        }

        // Add the new dirty rect to the current one
        localDirty.union(dirty.left, dirty.top, dirty.right, dirty.bottom);
        // Intersect with the bounds of the window to skip
        // updates that lie outside of the visible region
        final float appScale = mAttachInfo.mApplicationScale;
        final boolean intersected = localDirty.intersect(0, 0,
                (int) (mWidth * appScale + 0.5f), (int) (mHeight * appScale + 0.5f));
        if (!intersected) {
            localDirty.setEmpty();
        }
        if (!mWillDrawSoon && (intersected || mIsAnimating)) {
            // 调用 scheduleTraversals 进行整个绘制流程
            scheduleTraversals();
        }
    }
```

最终调用 scheduleTraversals 去触发整个绘制流程，然后调用到 View.draw 方法，根据 PFLAG_DIRTY_OPAQUE flag 去决定是否重新绘制。   

#### 12. requestLayout 和 invalidate 的区别
看完上面的 requestLayout 和 invalidate 的流程，我们就能明白他们之间的区别了。   

requestLayout 和 invalidate 都会触发整个绘制流程。但是在 measure 和 layout 过程中，只会对 flag 设置为 FORCE_LAYOUT 的情况进行重新测量和布局，而 draw 只会重绘 flag 为 dirty 的区域。   

requestLayout 是用来设置 FORCE_LAYOUT 标志，invalidate 用来设置 dirty 标志。所以 requestLayout 只会触发 measure 和 layout，invalidate 只会触发 draw。      

### 四、总结
上面就是我对 View 的绘制流程引申出的一些知识点的分析，当然并没有列举全，还有很多点可以去深入分析，只是提供一些思路。   

这里想说明的一点是，这些知识点并非没有用，比如 LayoutInflate.inflate 的流程中，看到的 Factory2 的设置，在自定义 View 解析的时候就很有用（之前在腾讯的一个项目中就有用到）。   

还想说明的是，面试官提问这些知识点，并非要全部答对，更多的是考察在工作中是否有深入了解，在深入了解源码的过程中是否有一些自己的思考，毕竟了解原理是创新的基础。   

本期文章就结束了，下期我们继续分析 View 的事件分发机制。   

> 文章持续更新，微信搜索「ZYLAB」第一时间获取更新，回复【模拟面试】，解锁大厂一对一面试体验