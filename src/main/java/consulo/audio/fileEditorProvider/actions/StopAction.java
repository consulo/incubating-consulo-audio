package consulo.audio.fileEditorProvider.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import consulo.audio.engine.AudioPlayer;
import consulo.audio.fileEditorProvider.AudioEditorKeys;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.annotation.RequiredUIAccess;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-11-12
 */
public class StopAction extends DumbAwareAction
{
	public StopAction()
	{
		super("Stop", null, PlatformIconGroup.actionsSuspend());
	}

	@RequiredUIAccess
	@Override
	public void actionPerformed(@Nonnull AnActionEvent e)
	{
		AudioPlayer player = e.getData(AudioEditorKeys.AUDIO_PLAYER);

		if(player != null)
		{
			player.stop();
		}
	}

	@RequiredUIAccess
	@Override
	public void update(@Nonnull AnActionEvent e)
	{
		AudioPlayer player = e.getData(AudioEditorKeys.AUDIO_PLAYER);
		Presentation presentation = e.getPresentation();

		presentation.setEnabled(player != null);
	}
}
