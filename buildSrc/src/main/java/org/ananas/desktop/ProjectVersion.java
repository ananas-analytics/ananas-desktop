package org.ananas.desktop;

import java.text.MessageFormat;

class ProjectVersion {
	public int major;
	public int minor;
	public int patch;
	public boolean release;

	public ProjectVersion(int major, int minor, int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.release = false;
	}

	public ProjectVersion(int major, int minor, int patch, boolean release) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.release = release;
	}

	@Override
	public String toString() {
		if (this.release) {
			return MessageFormat.format("{0}.{1}.{2}", this.major, this.minor, this.patch ); 
		}
		return MessageFormat.format("{0}.{1}.{2}-SNAPSHOT", this.major, this.minor, this.patch);
	}
}
