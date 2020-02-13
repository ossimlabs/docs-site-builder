package com.maxar.workflow.types;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class CesiumRequest
{
	protected String url;
	protected Object body;
	protected String parent;
	protected boolean generateParent;
	protected boolean displayInTree;
}
