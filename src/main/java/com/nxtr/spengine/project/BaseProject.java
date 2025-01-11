package com.nxtr.spengine.project;

import java.util.List;

import com.ngeneration.miengine.math.Vector2I;

import lombok.Data;

@Data
public class BaseProject {

	private String prjVersion;
	private String name;
	private String version;
	private int type;
	private Vector2I dimension;
	private String initialScene;
	private List<String> recents;

}
