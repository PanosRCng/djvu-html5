package pl.djvuhtml5.client;

import pl.djvuhtml5.client.PageCache.PageDownloadListener;
import pl.djvuhtml5.client.TileCache.TileCacheListener;
import pl.djvuhtml5.client.TileCache.TileInfo;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;
import com.lizardtech.djvu.DjVuInfo;
import com.lizardtech.djvu.DjVuPage;
import com.lizardtech.djvu.Document;

public class SinglePageLayout implements PageDownloadListener, TileCacheListener {

	private float zoom = 1;

	private int page;

	private DjVuInfo pageInfo;

	private int centerX, centerY;

	final TileCache tileCache;

	private final PageCache pageCache;

	private final Canvas canvas;

	private String background;
	
	private final int fitToPageMargin;

	private TileInfo tileInfoTemp = new TileInfo();

	public SinglePageLayout(Canvas canvas, Document document) {
		this.pageCache = new PageCache(document);
		pageCache.addPageDownloadListener(this);
		this.tileCache = new TileCache(pageCache);
		tileCache.addTileCacheListener(this);
		this.canvas = canvas;

		this.background = DjvuContext.getBackground();
		this.fitToPageMargin = DjvuContext.getFitToPageMargin() * 2;
	}

	public void setPage(int pageNum) {
		DjVuPage currentPage = pageCache.getPage(pageNum);
		if (currentPage != null) {
			pageInfo = currentPage.getInfo();
			page = pageNum;
			if (centerX == 0)
				zoomToFitPage();
		}
	}

	public int getPage() {
		return page;
	}

	public void canvasResized() {
		zoomToFitPage();
	}

	public void zoomToFitPage() {
		if (pageInfo == null)
			return;
		zoom = Math.min((1.0f * canvas.getCoordinateSpaceWidth() - fitToPageMargin) / pageInfo.width,
				(1.0f * canvas.getCoordinateSpaceHeight() - fitToPageMargin) / pageInfo.height);

		centerX = (int)(pageInfo.width * zoom) / 2;
		centerY = (int)(pageInfo.height * zoom) / 2;
	}

	public void redraw() {
		Context2d graphics2d = canvas.getContext2d();
		int w = canvas.getCoordinateSpaceWidth(), h = canvas.getCoordinateSpaceHeight();
		if (pageInfo == null) {
			graphics2d.setFillStyle(background);
			graphics2d.fillRect(0, 0, w, h);
			return;
		}
		int pw = (int) (pageInfo.width * zoom), ph = (int) (pageInfo.height * zoom);
		if (pw < w || ph < h) {
			graphics2d.setFillStyle(background);
			graphics2d.fillRect(0, 0, w, h);
		}
		double scale = 1 / tileCache.getScale(zoom);
		graphics2d.save();
		graphics2d.scale(scale, scale);
		int tileSize = tileCache.tileSize;
		int fromX = Math.max(0, centerX - w / 2) / tileSize;
		int toX = Math.min(pw, centerX + w / 2) / tileSize;
		int fromY = Math.max(0, centerY - h / 2) / tileSize;
		int toY = Math.min(ph, centerY + h / 2) / tileSize;
		tileInfoTemp.page = page;
		tileInfoTemp.subsample = tileCache.getSubsample(zoom);
		for (int  x = fromX; x <= toX; x++) {
			for (int y = fromY; y <= toY; y++) {
				tileInfoTemp.x = x;
				tileInfoTemp.y = y;
				Image tileImage = tileCache.getTileImage(tileInfoTemp);
				graphics2d.drawImage(ImageElement.as(tileImage.getElement()),
						x * tileSize - centerX + w / 2, y * tileSize - centerY + h / 2);
			}
		}
//		int pageEndX = pw - centerX + w / 2, pageEndY = ph - centerY + h / 2;
//		graphics2d.fillRect(pageEndX, 0, w - pageEndX, h);
//		graphics2d.fillRect(0, pageEndY, w, h - pageEndY);
		graphics2d.restore();
	}

	@Override
	public void pageAvailable(int pageNum) {
		if (pageNum == page) {
			setPage(pageNum);
			redraw();
		}
	}

	@Override
	public void tileAvailable(TileInfo tileInfo) {
		if (tileInfo.page == page)
			redraw();
	}
}