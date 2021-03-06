package org.jenkinsci.plugins.pretestedintegration;

import org.jenkinsci.plugins.pretestedintegration.exceptions.EstablishWorkspaceException;
import org.jenkinsci.plugins.pretestedintegration.exceptions.IntegationFailedExeception;
import org.jenkinsci.plugins.pretestedintegration.exceptions.NextCommitFailureException;
import org.jenkinsci.plugins.pretestedintegration.exceptions.NothingToDoException;
import java.io.IOException;
import java.util.logging.Logger;

import hudson.Launcher;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

public class PretestedIntegrationAction implements Action {

    AbstractBuild<?, ?> build;    
    AbstractSCMBridge scmBridge;
    Commit<?> last;
    Commit<?> commit;
    private Commit<?> currentIntegrationTip;

    public PretestedIntegrationAction(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, AbstractSCMBridge scmBridge) throws NextCommitFailureException {
        this.build = build;
        this.scmBridge = scmBridge;
        this.currentIntegrationTip = scmBridge.determineIntegrationHead(build, launcher, listener);
        try {
            this.last = build.getPreviousBuiltBuild().getAction(PretestedIntegrationAction.class).getCommit();
        } catch (NullPointerException e) {
            last = null;
        }
        this.commit = scmBridge.nextCommit(build, launcher, listener, last);
    }
    


    public String getDisplayName() {
        return null;
    }

    public String getIconFileName() {
        return null;
    }

    public String getUrlName() {
        return "pretested-integration";
    }

    public Commit<?> getCommit() {
        return this.commit;
    }

    /**
     * Invoked before the build is started, responsible for preparing the
     * workspace
     *
     * @param launcher
     * @param listener
     * @return True if any changes are made and the workspace has been prepared,
     * false otherwise
     * @throws IntegationFailedExeception
     * @throws NothingToDoException
     * @throws EstablishWorkspaceException    
     */
    public boolean initialise(Launcher launcher, BuildListener listener) throws IntegationFailedExeception, NothingToDoException, EstablishWorkspaceException {
        boolean result = false;
        
        if (commit != null) {
            result = true;
            scmBridge.prepareWorkspace(build, launcher, listener, commit);
        }
        return result;
    }

    /**
     * Invoked by the notifier, responsible for commiting or rolling back the
     * workspace
     *
     * @param launcher
     * @param listener
     * @throws IllegalArgumentException
     * @throws IOException
     * @return {@link Boolean} indicating success or failure
     
     */
    public boolean finalise(Launcher launcher, BuildListener listener) throws IOException {
        scmBridge.handlePostBuild(build, launcher, listener);
        return true;
    }

    private static final Logger logger = Logger.getLogger(PretestedIntegrationAction.class.getName());

    /**
     * @return the currentIntegrationTip
     */
    public Commit<?> getCurrentIntegrationTip() {
        return currentIntegrationTip;
    }

    /**
     * @param currentIntegrationTip the currentIntegrationTip to set
     */
    public void setCurrentIntegrationTip(Commit<?> currentIntegrationTip) {
        this.currentIntegrationTip = currentIntegrationTip;
    }
}
