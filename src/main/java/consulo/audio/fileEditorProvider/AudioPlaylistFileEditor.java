package consulo.audio.fileEditorProvider;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.util.PathUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import consulo.audio.playlist.AudioPlaylistStore;
import consulo.disposer.Disposer;
import consulo.logging.Logger;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import consulo.util.dataholder.UserDataHolderBase;
import kava.beans.PropertyChangeListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author VISTALL
 * @since 2020-11-12
 */
public class AudioPlaylistFileEditor extends UserDataHolderBase implements FileEditor
{
	private static final Logger LOG = Logger.getInstance(AudioPlaylistFileEditor.class);

	private final Project myProject;

	private JPanel myRoot;

	private Map<String, AudioPlayerWrapper> myLoadedFiles = new ConcurrentHashMap<>();

	private CollectionListModel<AudioPlayerWrapper> myModel;

	private AudioPlayerUI myAudioPlayerUI;
	private JBList<AudioPlayerWrapper> myList;

	public AudioPlaylistFileEditor(Project project, VirtualFile virtualFile)
	{
		myProject = project;
	}

	@Nonnull
	public JComponent getComponent()
	{
		if(myRoot != null)
		{
			return myRoot;
		}

		myRoot = new JPanel(new BorderLayout());

		OnePixelSplitter splitter = new OnePixelSplitter("AudioPlaylistFileEditor", 0.7f);
		myRoot.add(splitter, BorderLayout.CENTER);

		myModel = new CollectionListModel<>();
		myList = new JBList<>(myModel);

		AudioPlaylistStore playlistStore = AudioPlaylistStore.getInstance(myProject);

		Collection<String> files = playlistStore.getFiles();
		for(String fileUrl : files)
		{
			AudioPlayerWrapper wrapper = new AudioPlayerWrapper(fileUrl);
			myLoadedFiles.put(fileUrl, wrapper);
			myModel.add(wrapper);
		}

		myList.addListSelectionListener(listSelectionEvent ->
		{
			AudioPlayerWrapper value = myList.getSelectedValue();
			myAudioPlayerUI.update(value);
		});

		myList.setCellRenderer((list, playerWrapper, index, isSelected, cellHasFocus) -> {
			JPanel panel = new JPanel(new BorderLayout())
			{
				@Override
				public Dimension getPreferredSize()
				{
					Dimension preferredSize = super.getPreferredSize();
					return new Dimension(preferredSize.width, JBUI.scale(50));
				}
			};

			panel.setBorder(JBUI.Borders.empty(0, 5));
			panel.setBackground(UIUtil.getListBackground(isSelected));

			Image icon = PlatformIconGroup.fileTypesUnknown();
			String fileRelativePath = "";
			String filePath = FileUtil.toSystemDependentName(playerWrapper.getFilePath());

			VirtualFile file = playerWrapper.getFile();
			if(file != null)
			{
				String relativeLocation = VfsUtil.getRelativeLocation(file, myProject.getBaseDir());
				if(relativeLocation != null)
				{
					fileRelativePath = relativeLocation;
				}
				else
				{
					fileRelativePath = file.getPath();
				}

				icon = file.getFileType().getIcon();
			}
			else
			{
				fileRelativePath = PathUtil.getFileName(filePath);
			}

			JBLabel label = new JBLabel(icon);
			label.setOpaque(false);

			panel.add(label, BorderLayout.WEST);

			SimpleColoredComponent relativePathComp = new SimpleColoredComponent();
			relativePathComp.setForeground(UIUtil.getListForeground(isSelected));
			relativePathComp.setTextAlign(SwingConstants.LEFT);
			relativePathComp.setOpaque(false);
			relativePathComp.append(fileRelativePath);

			SimpleColoredComponent pathComp = new SimpleColoredComponent();
			pathComp.setTextAlign(SwingConstants.LEFT);
			pathComp.setOpaque(false);
			if(isSelected)
			{
				pathComp.append(filePath);
				pathComp.setForeground(UIUtil.getListForeground(true));
			}
			else if(file == null)
			{
				pathComp.append(filePath, new SimpleTextAttributes(SimpleTextAttributes.STYLE_SMALLER, JBColor.RED));
			}
			else
			{
				pathComp.append(filePath, SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES);
			}

			JPanel vertial = new JPanel(new VerticalFlowLayout(true, true));
			vertial.setOpaque(false);
			vertial.add(relativePathComp);
			vertial.add(pathComp);

			panel.add(vertial, BorderLayout.CENTER);

			return panel;
		});

		myAudioPlayerUI = new AudioPlayerUI(playlistStore);
		Disposer.register(this, myAudioPlayerUI);

		splitter.setFirstComponent(ScrollPaneFactory.createScrollPane(myList, true));
		splitter.setSecondComponent(myAudioPlayerUI.getComponent());

		return myRoot;
	}

	public void addFile(@Nonnull VirtualFile file)
	{
		AudioPlayerWrapper wrapper = myLoadedFiles.computeIfAbsent(file.getUrl(), s ->
		{
			AudioPlaylistStore playlistStore = AudioPlaylistStore.getInstance(myProject);

			playlistStore.addFile(file);

			AudioPlayerWrapper w = new AudioPlayerWrapper(file.getUrl());

			myModel.add(w);

			return w;
		});

		myList.setSelectedValue(wrapper, true);
	}

	@Nullable
	@Override
	public JComponent getPreferredFocusedComponent()
	{
		return myRoot;
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "audio";
	}

	@Nonnull
	@Override
	public FileEditorState getState(@Nonnull FileEditorStateLevel fileEditorStateLevel)
	{
		return FileEditorState.INSTANCE;
	}

	@Override
	public void setState(@Nonnull FileEditorState fileEditorState)
	{

	}

	@Override
	public boolean isModified()
	{
		return false;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public void selectNotify()
	{

	}

	@Override
	public void deselectNotify()
	{

	}

	@Override
	public void addPropertyChangeListener(@Nonnull PropertyChangeListener propertyChangeListener)
	{

	}

	@Override
	public void removePropertyChangeListener(@Nonnull PropertyChangeListener propertyChangeListener)
	{

	}

	@Nullable
	@Override
	public FileEditorLocation getCurrentLocation()
	{
		return null;
	}

	@Override
	public void dispose()
	{
		for(AudioPlayerWrapper wrapper : myLoadedFiles.values())
		{
			Disposer.dispose(wrapper);
		}
	}
}
