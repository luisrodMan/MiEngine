package com.nxtr.spengine.views.inspector.resolver;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.nxtr.easymng.hearachy.Item;
import com.nxtr.easymng.workspace.Resource;

public class ContentManager<R, A extends Content<R>, T extends ContentProvider<R, A>> {

	private Set<ContentProvider<R, A>> contentResolvers = new LinkedHashSet<>();

	public void addResolver(ContentProvider<R, A> resolver) {
		contentResolvers.add(resolver);
	}

	public void addFileResolver(String exts, Function<Resource, R> consumer) {
		addResolver(new ResourceContentProvider<R, A>(exts) {
			@SuppressWarnings("unchecked")
			@Override
			protected A resolveResource(Resource item) {
				return (A) consumer.apply(item);
			}
		});

	}

	public void removeContentResolver(T resolver) {
		contentResolvers.remove(resolver);
	}

	public ContentProvider<R, A> getResolver(Item item) {
		return contentResolvers.stream()
				.filter(r -> List.of(r.getTypes()).stream().anyMatch(t -> t.isAssignableFrom(item.getClass())))
				.filter(r -> {
					if (r instanceof ResourceContentProvider ir) {
						if (!ir.canHandleExt(((Resource) item).getExt()))
							return false;
					}
					return true;
				}).findAny().orElse(null);
	}

	public A getContent(Item item) {
		var resolver = getResolver(item);
		return resolver == null ? null : resolver.resolve(item);
	}

}
