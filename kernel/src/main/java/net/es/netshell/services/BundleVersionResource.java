/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.es.netshell.services;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

/**
 *
 * @author hacksaw
 */
public class BundleVersionResource {
    private String id;                      // Unique identifier for the bundle.
    private String href;                    // A URI reference to the resource.

    // Github tags.
    private String tags;                    // =${git.tags} // comma separated tag names
    private String branch;                  // =${git.branch}
    private String dirty;                   // =${git.dirty}
    private String remoteOriginUrl;         // =${git.remote.origin.url}

    private String commitId;                // =${git.commit.id.full} OR ${git.commit.id}
    private String commitIdAbbrev;          // =${git.commit.id.abbrev}
    private String describe;                // =${git.commit.id.describe}
    private String describeShort;           // =${git.commit.id.describe-short}
    private String commitUserName;          // =${git.commit.user.name}
    private String commitUserEmail;         // =${git.commit.user.email}
    private String commitMessageFull;       // =${git.commit.message.full}
    private String commitMessageShort;      // =${git.commit.message.short}
    private String commitTime;              // =${git.commit.time}
    private String closestTagName;          // =${git.closest.tag.name}
    private String closestTagCommitCount;   // =${git.closest.tag.commit.count}

    private String buildUserName;           // =${git.build.user.name}
    private String buildUserEmail;          // =${git.build.user.email}
    private String buildTime;               // =${git.build.time}
    private String buildHost;               // =${git.build.host}
    private String buildVersion;            // =${git.build.version}

    // OSGi bundle tags.
    private String createdBy;               // Created-By
    private String builtBy;                 // Built-By
    private String tool;                    // Tool
    private String buildJdk;                // Build-Jdk

    private String provideCapability;       // Provide-Capability
    private String requireCapability;       // Require-Capability
    private String importPackage;           // Import-Package
    private String exportPackage;           // Export-Package
    private String serviceComponent;        // Service-Component

    private String bndLastModified;         // Bnd-LastModified
    private String bundleDocURL;            // Bundle-DocURL
    private String bundleLicense;           // Bundle-License
    private String bundleManifestVersion;   // Bundle-ManifestVersion
    private String bundleName;              // Bundle-Name
    private String bundleSymbolicName;      // Bundle-SymbolicName
    private String bundleVendor;            // Bundle-Vendor
    private String bundleVersion;           // Bundle-Version
    private String manifestVersion;         // Manifest-Version

    public BundleVersionResource(Properties properties) {
        // Populate the Github properties.
        this.tags = String.valueOf(properties.get("git.tags"));
        this.branch = String.valueOf(properties.get("git.branch"));
        this.dirty = String.valueOf(properties.get("git.dirty"));
        this.remoteOriginUrl = String.valueOf(properties.get("git.remote.origin.url"));

        this.commitId = String.valueOf(properties.get("git.commit.id.full")); // OR properties.get("git.commit.id") depending on your configuration
        this.commitIdAbbrev = String.valueOf(properties.get("git.commit.id.abbrev"));
        this.describe = String.valueOf(properties.get("git.commit.id.describe"));
        this.describeShort = String.valueOf(properties.get("git.commit.id.describe-short"));
        this.commitUserName = String.valueOf(properties.get("git.commit.user.name"));
        this.commitUserEmail = String.valueOf(properties.get("git.commit.user.email"));
        this.commitMessageFull = String.valueOf(properties.get("git.commit.message.full"));
        this.commitMessageShort = String.valueOf(properties.get("git.commit.message.short"));
        this.commitTime = String.valueOf(properties.get("git.commit.time"));
        this.closestTagName = String.valueOf(properties.get("git.closest.tag.name"));
        this.closestTagCommitCount = String.valueOf(properties.get("git.closest.tag.commit.count"));

        this.buildUserName = String.valueOf(properties.get("git.build.user.name"));
        this.buildUserEmail = String.valueOf(properties.get("git.build.user.email"));
        this.buildTime = String.valueOf(properties.get("git.build.time"));
        this.buildHost = String.valueOf(properties.get("git.build.host"));
        this.buildVersion = String.valueOf(properties.get("git.build.version"));

        // Populate the OSGi properties.
        this.manifestVersion = String.valueOf(properties.get("Manifest-Version"));
        this.bundleVendor = String.valueOf(properties.get("Bundle-Vendor"));
        this.createdBy = String.valueOf(properties.get("Created-By"));
        this.bundleManifestVersion = String.valueOf(properties.get("Bundle-ManifestVersion"));
        this.tool = String.valueOf(properties.get("Tool"));
        this.provideCapability = String.valueOf(properties.get("Provide-Capability"));
        this.bundleSymbolicName = String.valueOf(properties.get("Bundle-SymbolicName"));
        this.exportPackage = String.valueOf(properties.get("Export-Package"));
        this.bundleVersion = String.valueOf(properties.get("Bundle-Version"));
        this.bundleName = String.valueOf(properties.get("Bundle-Name"));
        this.importPackage = String.valueOf(properties.get("Import-Package"));
        this.requireCapability = String.valueOf(properties.get("Require-Capability"));
        this.bndLastModified = getServerTime(String.valueOf(properties.get("Bnd-LastModified")));
        this.buildJdk = String.valueOf(properties.get("Build-Jdk"));
        this.bundleLicense = String.valueOf(properties.get("Bundle-License"));
        this.serviceComponent = String.valueOf(properties.get("Service-Component"));
        this.builtBy = String.valueOf(properties.get("Built-By"));
        this.bundleDocURL = String.valueOf(properties.get("Bundle-DocURL"));

        this.id = this.bundleSymbolicName + ":" + this.bundleVersion;
    }

    private String getServerTime(String time) {
        long aLong = 0;
        try {
            aLong = Long.parseLong(time);
        }
        catch (NumberFormatException ex) { }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(aLong);
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the href
     */
    public String getHref() {
        return href;
    }

    /**
     * @param href the href to set
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * @return the tags
     */
    public String getTags() {
        return tags;
    }

    /**
     * @param tags the tags to set
     */
    public void setTags(String tags) {
        this.tags = tags;
    }

    /**
     * @return the branch
     */
    public String getBranch() {
        return branch;
    }

    /**
     * @param branch the branch to set
     */
    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * @return the dirty
     */
    public String getDirty() {
        return dirty;
    }

    /**
     * @param dirty the dirty to set
     */
    public void setDirty(String dirty) {
        this.dirty = dirty;
    }

    /**
     * @return the remoteOriginUrl
     */
    public String getRemoteOriginUrl() {
        return remoteOriginUrl;
    }

    /**
     * @param remoteOriginUrl the remoteOriginUrl to set
     */
    public void setRemoteOriginUrl(String remoteOriginUrl) {
        this.remoteOriginUrl = remoteOriginUrl;
    }

    /**
     * @return the commitId
     */
    public String getCommitId() {
        return commitId;
    }

    /**
     * @param commitId the commitId to set
     */
    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    /**
     * @return the commitIdAbbrev
     */
    public String getCommitIdAbbrev() {
        return commitIdAbbrev;
    }

    /**
     * @param commitIdAbbrev the commitIdAbbrev to set
     */
    public void setCommitIdAbbrev(String commitIdAbbrev) {
        this.commitIdAbbrev = commitIdAbbrev;
    }

    /**
     * @return the describe
     */
    public String getDescribe() {
        return describe;
    }

    /**
     * @param describe the describe to set
     */
    public void setDescribe(String describe) {
        this.describe = describe;
    }

    /**
     * @return the describeShort
     */
    public String getDescribeShort() {
        return describeShort;
    }

    /**
     * @param describeShort the describeShort to set
     */
    public void setDescribeShort(String describeShort) {
        this.describeShort = describeShort;
    }

    /**
     * @return the commitUserName
     */
    public String getCommitUserName() {
        return commitUserName;
    }

    /**
     * @param commitUserName the commitUserName to set
     */
    public void setCommitUserName(String commitUserName) {
        this.commitUserName = commitUserName;
    }

    /**
     * @return the commitUserEmail
     */
    public String getCommitUserEmail() {
        return commitUserEmail;
    }

    /**
     * @param commitUserEmail the commitUserEmail to set
     */
    public void setCommitUserEmail(String commitUserEmail) {
        this.commitUserEmail = commitUserEmail;
    }

    /**
     * @return the commitMessageFull
     */
    public String getCommitMessageFull() {
        return commitMessageFull;
    }

    /**
     * @param commitMessageFull the commitMessageFull to set
     */
    public void setCommitMessageFull(String commitMessageFull) {
        this.commitMessageFull = commitMessageFull;
    }

    /**
     * @return the commitMessageShort
     */
    public String getCommitMessageShort() {
        return commitMessageShort;
    }

    /**
     * @param commitMessageShort the commitMessageShort to set
     */
    public void setCommitMessageShort(String commitMessageShort) {
        this.commitMessageShort = commitMessageShort;
    }

    /**
     * @return the commitTime
     */
    public String getCommitTime() {
        return commitTime;
    }

    /**
     * @param commitTime the commitTime to set
     */
    public void setCommitTime(String commitTime) {
        this.commitTime = commitTime;
    }

    /**
     * @return the closestTagName
     */
    public String getClosestTagName() {
        return closestTagName;
    }

    /**
     * @param closestTagName the closestTagName to set
     */
    public void setClosestTagName(String closestTagName) {
        this.closestTagName = closestTagName;
    }

    /**
     * @return the closestTagCommitCount
     */
    public String getClosestTagCommitCount() {
        return closestTagCommitCount;
    }

    /**
     * @param closestTagCommitCount the closestTagCommitCount to set
     */
    public void setClosestTagCommitCount(String closestTagCommitCount) {
        this.closestTagCommitCount = closestTagCommitCount;
    }

    /**
     * @return the buildUserName
     */
    public String getBuildUserName() {
        return buildUserName;
    }

    /**
     * @param buildUserName the buildUserName to set
     */
    public void setBuildUserName(String buildUserName) {
        this.buildUserName = buildUserName;
    }

    /**
     * @return the buildUserEmail
     */
    public String getBuildUserEmail() {
        return buildUserEmail;
    }

    /**
     * @param buildUserEmail the buildUserEmail to set
     */
    public void setBuildUserEmail(String buildUserEmail) {
        this.buildUserEmail = buildUserEmail;
    }

    /**
     * @return the buildTime
     */
    public String getBuildTime() {
        return buildTime;
    }

    /**
     * @param buildTime the buildTime to set
     */
    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    /**
     * @return the buildHost
     */
    public String getBuildHost() {
        return buildHost;
    }

    /**
     * @param buildHost the buildHost to set
     */
    public void setBuildHost(String buildHost) {
        this.buildHost = buildHost;
    }

    /**
     * @return the buildVersion
     */
    public String getBuildVersion() {
        return buildVersion;
    }

    /**
     * @param buildVersion the buildVersion to set
     */
    public void setBuildVersion(String buildVersion) {
        this.buildVersion = buildVersion;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @return the builtBy
     */
    public String getBuiltBy() {
        return builtBy;
    }

    /**
     * @param builtBy the builtBy to set
     */
    public void setBuiltBy(String builtBy) {
        this.builtBy = builtBy;
    }

    /**
     * @return the tool
     */
    public String getTool() {
        return tool;
    }

    /**
     * @param tool the tool to set
     */
    public void setTool(String tool) {
        this.tool = tool;
    }

    /**
     * @return the buildJdk
     */
    public String getBuildJdk() {
        return buildJdk;
    }

    /**
     * @param buildJdk the buildJdk to set
     */
    public void setBuildJdk(String buildJdk) {
        this.buildJdk = buildJdk;
    }

    /**
     * @return the provideCapability
     */
    public String getProvideCapability() {
        return provideCapability;
    }

    /**
     * @param provideCapability the provideCapability to set
     */
    public void setProvideCapability(String provideCapability) {
        this.provideCapability = provideCapability;
    }

    /**
     * @return the requireCapability
     */
    public String getRequireCapability() {
        return requireCapability;
    }

    /**
     * @param requireCapability the requireCapability to set
     */
    public void setRequireCapability(String requireCapability) {
        this.requireCapability = requireCapability;
    }

    /**
     * @return the importPackage
     */
    public String getImportPackage() {
        return importPackage;
    }

    /**
     * @param importPackage the importPackage to set
     */
    public void setImportPackage(String importPackage) {
        this.importPackage = importPackage;
    }

    /**
     * @return the exportPackage
     */
    public String getExportPackage() {
        return exportPackage;
    }

    /**
     * @param exportPackage the exportPackage to set
     */
    public void setExportPackage(String exportPackage) {
        this.exportPackage = exportPackage;
    }

    /**
     * @return the serviceComponent
     */
    public String getServiceComponent() {
        return serviceComponent;
    }

    /**
     * @param serviceComponent the serviceComponent to set
     */
    public void setServiceComponent(String serviceComponent) {
        this.serviceComponent = serviceComponent;
    }

    /**
     * @return the bndLastModified
     */
    public String getBndLastModified() {
        return bndLastModified;
    }

    /**
     * @param bndLastModified the bndLastModified to set
     */
    public void setBndLastModified(String bndLastModified) {
        this.bndLastModified = bndLastModified;
    }

    /**
     * @return the bundleDocURL
     */
    public String getBundleDocURL() {
        return bundleDocURL;
    }

    /**
     * @param bundleDocURL the bundleDocURL to set
     */
    public void setBundleDocURL(String bundleDocURL) {
        this.bundleDocURL = bundleDocURL;
    }

    /**
     * @return the bundleLicense
     */
    public String getBundleLicense() {
        return bundleLicense;
    }

    /**
     * @param bundleLicense the bundleLicense to set
     */
    public void setBundleLicense(String bundleLicense) {
        this.bundleLicense = bundleLicense;
    }

    /**
     * @return the bundleManifestVersion
     */
    public String getBundleManifestVersion() {
        return bundleManifestVersion;
    }

    /**
     * @param bundleManifestVersion the bundleManifestVersion to set
     */
    public void setBundleManifestVersion(String bundleManifestVersion) {
        this.bundleManifestVersion = bundleManifestVersion;
    }

    /**
     * @return the bundleName
     */
    public String getBundleName() {
        return bundleName;
    }

    /**
     * @param bundleName the bundleName to set
     */
    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    /**
     * @return the bundleSymbolicName
     */
    public String getBundleSymbolicName() {
        return bundleSymbolicName;
    }

    /**
     * @param bundleSymbolicName the bundleSymbolicName to set
     */
    public void setBundleSymbolicName(String bundleSymbolicName) {
        this.bundleSymbolicName = bundleSymbolicName;
    }

    /**
     * @return the bundleVendor
     */
    public String getBundleVendor() {
        return bundleVendor;
    }

    /**
     * @param bundleVendor the bundleVendor to set
     */
    public void setBundleVendor(String bundleVendor) {
        this.bundleVendor = bundleVendor;
    }

    /**
     * @return the bundleVersion
     */
    public String getBundleVersion() {
        return bundleVersion;
    }

    /**
     * @param bundleVersion the bundleVersion to set
     */
    public void setBundleVersion(String bundleVersion) {
        this.bundleVersion = bundleVersion;
    }

    /**
     * @return the manifestVersion
     */
    public String getManifestVersion() {
        return manifestVersion;
    }

    /**
     * @param manifestVersion the manifestVersion to set
     */
    public void setManifestVersion(String manifestVersion) {
        this.manifestVersion = manifestVersion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ID: ");
        sb.append(this.id);

        sb.append("\n  Bundle:");
        sb.append("\n    bundleName=");
        sb.append(this.bundleName);
        sb.append("\n    bundleSymbolicName=");
        sb.append(this.bundleSymbolicName);
        sb.append("\n    bundleVersion=");
        sb.append(this.bundleVersion);
        sb.append("\n    bundleVendor=");
        sb.append(this.bundleVendor);
        sb.append("\n    bndLastModified=");
        sb.append(this.bndLastModified);
        sb.append("\n    bundleDocURL=");
        sb.append(this.bundleDocURL);
        sb.append("\n    bundleLicense=");
        sb.append(this.bundleLicense);
        sb.append("\n    createdBy=");
        sb.append(this.createdBy);
        sb.append("\n    builtBy=");
        sb.append(this.builtBy);
        sb.append("\n    tool=");
        sb.append(this.tool);
        sb.append("\n    buildJdk=");
        sb.append(this.buildJdk);
        sb.append("\n    provideCapability=");
        sb.append(this.provideCapability);
        sb.append("\n    requireCapability=");
        sb.append(this.requireCapability);
        sb.append("\n    importPackage=");
        sb.append(this.importPackage);
        sb.append("\n    exportPackage=");
        sb.append(this.exportPackage);
        sb.append("\n    serviceComponent=");
        sb.append(this.serviceComponent);
        sb.append("\n    bundleManifestVersion=");
        sb.append(this.bundleManifestVersion);
        sb.append("\n    manifestVersion=");
        sb.append(this.manifestVersion);

        sb.append("\n  Github:\n    tags=");
        sb.append(this.tags);
        sb.append("\n    branch=");
        sb.append(this.branch);
        sb.append("\n    dirty=");
        sb.append(this.dirty);
        sb.append("\n    remoteOriginUrl=");
        sb.append(this.remoteOriginUrl);
        sb.append("\n    commitId=");
        sb.append(this.commitId);
        sb.append("\n    describe=");
        sb.append(this.describe);
        sb.append("\n    commitUserName=");
        sb.append(this.commitUserName);
        sb.append("\n    commitMessageFull=");
        sb.append(this.commitMessageFull);
        sb.append("\n    commitUserEmail=");
        sb.append(this.commitUserEmail);
        sb.append("\n    commitTime=");
        sb.append(this.commitTime);
        sb.append("\n    buildUserName=");
        sb.append(this.buildUserName);
        sb.append("\n    buildUserEmail=");
        sb.append(this.buildUserEmail);
        sb.append("\n    buildTime=");
        sb.append(this.buildTime);
        sb.append("\n    buildHost=");
        sb.append(this.buildHost);
        sb.append("\n    buildVersion=");
        sb.append(this.buildVersion);

        return sb.toString();
    }
}
