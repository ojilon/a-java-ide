/*
 * Copyright (C) 2018 Tran Le Duy
 */

package com.duy.ide.javaide;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.duy.android.compiler.builder.BuildTask;
import com.duy.android.compiler.builder.IBuilder;
import com.duy.android.compiler.builder.JavaBuilder;
import com.duy.android.compiler.project.AndroidAppProject;
import com.duy.android.compiler.project.FileCollection;
import com.duy.android.compiler.project.JavaProject;
import com.duy.android.compiler.project.JavaProjectManager;
import com.duy.android.compiler.utils.ProjectUtils;
import com.duy.common.purchase.Premium;
import com.duy.ide.R;
import com.duy.ide.code.api.CodeFormatProvider;
import com.duy.ide.code.api.SuggestionProvider;
import com.duy.ide.diagnostic.DiagnosticContract;
import com.duy.ide.diagnostic.DiagnosticPresenter;
import com.duy.ide.diagnostic.model.Message;
import com.duy.ide.diagnostic.parser.PatternAwareOutputParser;
import com.duy.ide.editor.IEditorDelegate;
import com.duy.ide.javaide.diagnostic.parser.aapt.AaptOutputParser;
import com.duy.ide.javaide.diagnostic.parser.java.JavaOutputParser;
import com.duy.ide.javaide.editor.autocomplete.JavaAutoCompleteProvider;
import com.duy.ide.javaide.editor.format.JavaIdeCodeFormatProvider;
import com.duy.ide.javaide.menu.JavaMenuManager;
import com.duy.ide.javaide.run.action.BuildJarAction;
import com.duy.ide.javaide.run.activities.ExecuteActivity;
import com.duy.ide.javaide.run.dialog.DialogRunConfig;
import com.duy.ide.javaide.run.dialog.RunJarDialog;
import com.duy.ide.javaide.sample.activities.JavaSampleActivity;
import com.duy.ide.javaide.setting.CompilerSettingActivity;
import com.duy.ide.javaide.theme.ThemeActivity;
import com.duy.ide.javaide.uidesigner.inflate.DialogLayoutPreview;
import com.duy.ide.javaide.utils.StoreUtil;
import com.jecelyin.editor.v2.common.Command;
import com.jecelyin.editor.v2.manager.MenuManager;
import com.jecelyin.editor.v2.widget.menu.MenuDef;
import com.pluscubed.logcat.ui.LogcatActivity;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Main IDE activity — Java compile/run on device.
 * Android APK build path disabled (AOSP modules removed).
 * Billing / premium gates removed.
 */
public class JavaIdeActivity extends ProjectManagerActivity implements DialogRunConfig.OnConfigChangeListener {
    private static final String TAG = "MainActivity";

    private static final int RC_OPEN_SAMPLE = 1015;
    private static final int RC_BUILD_PROJECT = 131;
    private static final int RC_REVIEW_LAYOUT = 741;
    private static final int RC_CHANGE_THEME = 350;
    private static final int RC_BUILD_JAR = 799;

    private ProgressBar mCompileProgress;
    private SuggestionProvider mAutoCompleteProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCompileProgress = findViewById(R.id.compile_progress);
        startAutoCompleteService();
    }

    @Override
    protected void populateDiagnostic(@NonNull DiagnosticContract.Presenter diagnosticPresenter) {
        PatternAwareOutputParser[] parsers = new PatternAwareOutputParser[]{
                new AaptOutputParser(),
                new JavaOutputParser()
        };
        diagnosticPresenter.setOutputParser(parsers);
        diagnosticPresenter.setFilter(message ->
                message.getKind() == Message.Kind.ERROR
                        || message.getKind() == Message.Kind.WARNING);
    }

    @Override
    public void onEditorViewCreated(@NonNull IEditorDelegate editorDelegate) {
        super.onEditorViewCreated(editorDelegate);
        editorDelegate.setSuggestionProvider(mAutoCompleteProvider);
    }

    @Override
    public void onEditorViewDestroyed(@NonNull IEditorDelegate editorDelegate) {
        super.onEditorViewDestroyed(editorDelegate);
    }

    private void populateAutoCompleteService(@NonNull SuggestionProvider provider) {
        for (IEditorDelegate delegate : getTabManager().getEditorPagerAdapter().getAllEditor()) {
            if (delegate != null) {
                delegate.setSuggestionProvider(provider);
            }
        }
    }

    @Override
    protected CodeFormatProvider getCodeFormatProvider() {
        return new JavaIdeCodeFormatProvider(this);
    }

    protected void startAutoCompleteService() {
        Log.d(TAG, "startAutoCompleteService() called");
        if (mAutoCompleteProvider == null) {
            if (mProject != null) {
                new Thread(() -> {
                    JavaAutoCompleteProvider provider = new JavaAutoCompleteProvider(JavaIdeActivity.this);
                    provider.load(mProject);
                    mAutoCompleteProvider = provider;
                    runOnUiThread(() -> populateAutoCompleteService(mAutoCompleteProvider));
                }).start();
            }
        } else {
            populateAutoCompleteService(mAutoCompleteProvider);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu container) {
        container.add(0, R.id.action_run, 0, R.string.run)
                .setIcon(MenuManager.makeToolbarNormalIcon(this,
                        R.drawable.ic_play_arrow_white_24dp))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(container);
        MenuItem fileMenu = container.findItem(R.id.menu_file);
        if (fileMenu != null) {
            new JavaMenuManager(this).createFileMenu(fileMenu.getSubMenu());
        }
        return true;
    }

    @Override
    protected void onCreateNavigationMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nav_javaide, menu);
        MenuItem premium = menu.findItem(R.id.action_premium);
        if (premium != null) {
            premium.setVisible(false);
        }
        super.onCreateNavigationMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_run) {
            saveAll(RC_BUILD_PROJECT);
        } else if (id == R.id.action_new_java_project) {
            createJavaProject();
        } else if (id == R.id.action_new_android_project) {
            createAndroidProject();
        } else if (id == R.id.action_new_file) {
            createNewFile(null);
        } else if (id == R.id.action_new_class) {
            createNewClass(null);
        } else if (id == R.id.action_open_java_project) {
            openJavaProject();
        } else if (id == R.id.action_open_android_project) {
            openAndroidProject();
        } else if (id == R.id.action_sample) {
            startActivityForResult(
                    new Intent(this, JavaSampleActivity.class),
                    JavaIdeActivity.RC_OPEN_SAMPLE);
        } else if (id == R.id.action_see_logcat) {
            startActivity(new Intent(this, LogcatActivity.class));
        } else if (id == R.id.action_compiler_setting) {
            startActivity(new Intent(this, CompilerSettingActivity.class));
            return true;
        } else if (id == R.id.action_install_cpp_nide) {
            StoreUtil.gotoPlayStore(this, "com.duy.c.cpp.compiler");
        } else if (id == R.id.action_report_bug) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/tranleduy2000/javaide/issues"));
            startActivity(intent);
        } else if (id == R.id.action_premium) {
            Toast.makeText(this, R.string.title_premium_version, Toast.LENGTH_SHORT).show();
        } else if (id == R.id.action_editor_color_scheme) {
            startActivityForResult(new Intent(this, ThemeActivity.class), RC_CHANGE_THEME);
        } else if (id == R.id.action_build_jar) {
            saveAll(RC_BUILD_JAR);
        } else if (id == R.id.action_run_jar) {
            runJar();
        }
        return super.onOptionsItemSelected(item);
    }

    private void runJar() {
        RunJarDialog runJarDialog = RunJarDialog.newInstance();
        runJarDialog.show(getSupportFragmentManager(), RunJarDialog.class.getName());
    }

    @Override
    protected void onSaveComplete(int requestCode) {
        super.onSaveComplete(requestCode);
        switch (requestCode) {
            case RC_BUILD_PROJECT:
                if (mProject != null) {
                    if (mProject instanceof AndroidAppProject) {
                        compileAndroidProject();
                    } else {
                        compileJavaProject();
                    }
                } else {
                    Toast.makeText(this, "You need create project", Toast.LENGTH_SHORT).show();
                }
                break;
            case RC_REVIEW_LAYOUT:
                File currentFile = getCurrentFile();
                if (currentFile != null) {
                    DialogLayoutPreview dialogPreview = DialogLayoutPreview.newInstance(currentFile);
                    dialogPreview.show(getSupportFragmentManager(), DialogLayoutPreview.TAG);
                } else {
                    Toast.makeText(this, "Can not find file", Toast.LENGTH_SHORT).show();
                }
                break;
            case RC_BUILD_JAR:
                if (mProject != null) {
                    new BuildJarAction(mProject).execute(this);
                }
                break;
        }
    }

    /**
     * Android APK packaging depended on the removed AOSP modules.
     * Java projects still compile and run on-device.
     */
    private void compileAndroidProject() {
        Toast.makeText(this,
                "Android APK build is disabled in this phone-first build. Use a Java project instead.",
                Toast.LENGTH_LONG).show();
    }

    private void compileJavaProject() {
        final IBuilder<JavaProject> builder = new JavaBuilder(this, mProject);
        builder.setStdOut(new PrintStream(mDiagnosticPresenter.getStandardOutput()));
        builder.setStdErr(new PrintStream(mDiagnosticPresenter.getErrorOutput()));

        final BuildTask.CompileListener<JavaProject> listener = new BuildTask.CompileListener<JavaProject>() {
            @Override
            public void onStart() {
                updateUiStartCompile();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(JavaIdeActivity.this, R.string.failed_msg,
                        Toast.LENGTH_SHORT).show();
                mDiagnosticPresenter.showPanel();
                updateUIFinishCompile();
            }

            @Override
            public void onComplete() {
                updateUIFinishCompile();
                Toast.makeText(JavaIdeActivity.this, R.string.compile_success,
                        Toast.LENGTH_SHORT).show();
                runJava(mProject);
            }
        };
        BuildTask<JavaProject> buildTask = new BuildTask<>(builder, listener);
        buildTask.execute();
    }

    private void runJava(final JavaProject project) {
        final File currentFile = getCurrentFile();
        if (currentFile == null || !ProjectUtils.isFileBelongProject(project, currentFile)) {
            ArrayList<File> javaSrcDirs = new ArrayList<>();
            javaSrcDirs.add(project.getJavaSrcDir());
            FileCollection fileCollection = new FileCollection(javaSrcDirs);
            final ArrayList<File> javaSources = fileCollection.filter(pathname ->
                    pathname.isFile() && pathname.getName().endsWith(".java"));
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String[] names = new String[javaSources.size()];
            for (int i = 0; i < javaSources.size(); i++) {
                names[i] = javaSources.get(i).getName();
            }
            builder.setTitle(R.string.select_class_to_run);
            builder.setItems(names, (dialog, which) -> {
                Intent intent = new Intent(JavaIdeActivity.this, ExecuteActivity.class);
                intent.putExtra(ExecuteActivity.DEX_FILE, project.getDexFile());
                intent.putExtra(ExecuteActivity.MAIN_CLASS_FILE, javaSources.get(which));
                startActivity(intent);
            });
            builder.create().show();
        } else {
            Intent intent = new Intent(JavaIdeActivity.this, ExecuteActivity.class);
            intent.putExtra(ExecuteActivity.DEX_FILE, project.getDexFile());
            intent.putExtra(ExecuteActivity.MAIN_CLASS_FILE, currentFile);
            startActivity(intent);
        }
    }

    public void createNewFile(View view) {
        // not implemented in upstream either
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_OPEN_SAMPLE:
                if (resultCode == RESULT_OK && data != null) {
                    String projectPath = data.getStringExtra(JavaSampleActivity.PROJECT_PATH);
                    JavaProjectManager manager = new JavaProjectManager(this);
                    JavaProject javaProject = null;
                    try {
                        javaProject = manager.loadProject(new File(projectPath), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (javaProject != null) {
                        onProjectCreated(javaProject);
                    }
                }
                break;
            case RC_CHANGE_THEME:
                doCommandForAllEditor(new Command(Command.CommandEnum.REFRESH_THEME));
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public void previewLayout(String path) {
        saveAll(RC_REVIEW_LAYOUT);
    }

    @Override
    public void onConfigChange(JavaProject projectFile) {
        this.mProject = projectFile;
        if (projectFile != null) {
            JavaProjectManager.saveProject(this, projectFile);
        }
    }

    public void updateUiStartCompile() {
        setMenuStatus(R.id.action_run, MenuDef.STATUS_DISABLED);
        if (mCompileProgress != null) {
            mCompileProgress.setVisibility(View.VISIBLE);
        }

        mDiagnosticPresenter.setCurrentItem(DiagnosticContract.COMPILER_LOG);
        mDiagnosticPresenter.showPanel();
        mDiagnosticPresenter.clear();
    }

    public void updateUIFinishCompile() {
        setMenuStatus(R.id.action_run, MenuDef.STATUS_NORMAL);
        if (mCompileProgress != null) {
            mCompileProgress.setVisibility(View.GONE);
        }
    }

    public DiagnosticPresenter getDiagnosticPresenter() {
        return mDiagnosticPresenter;
    }
}
