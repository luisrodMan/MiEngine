package com.nxtr.spengine.views.inspector.resolver;

import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.workspace.Resource;

public abstract class ResourceContentProvider<T1, T2 extends Content<T1>> implements ContentProvider<T1, T2> {

	private String[] exts;

	public ResourceContentProvider(String extensions) {
		exts = extensions.split("\\s*,\\s*");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<? extends Item>[] getTypes() {
		return new Class[] { Resource.class };
	}

	public String[] getExts() {
		return exts;
	}

	@Override
	public T2 resolve(Item item) {
		var ext = ((Resource) item).getExt();
		return canHandleExt(ext) ? resolveResource((Resource) item) : null;
	}

	protected abstract T2 resolveResource(Resource item);

	public boolean canHandleExt(String ext) {
		for (var e : exts) {
			if (e.equalsIgnoreCase(ext)) {
				return true;
			}
		}
		return false;
	}

}
