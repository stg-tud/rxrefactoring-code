package ru.ifmo.juneiform;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import de.tudarmstadt.stg.rx.swingworker.SWEmitter;
import de.tudarmstadt.stg.rx.swingworker.SWPackage;
import de.tudarmstadt.stg.rx.swingworker.SWSubscriber;
import rx.Emitter;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.schedulers.SwingScheduler;

/**
 *
 * @author Ivan Stepuk
 */
public abstract class DocumentLoader extends SWSubscriber<List<Document>, Document>
{

	private static final Logger log = Logger.getLogger( DocumentLoader.class );
	private File[] files;
	private Settings settings;

	public DocumentLoader()
	{
		setObservable( getRxObservable() );
	}

	public DocumentLoader( Settings settings )
	{
		this();
		this.settings = settings;
	}

	public void load( File... files )
	{
		this.files = files;
		// setup subject
		ConnectableObservable<Document> connectableObservable = getRxObservable()
				.subscribeOn( Schedulers.computation() )
				.flatMap( swPackage -> Observable.from( swPackage.getChunks() ) )
				.filter( doc -> doc.getName().contains( ".jpg" ) )
				.map( doc -> new Document(
						doc.getName().replace( ".jpg", "" ).toUpperCase(),
						doc.getPath(),
						doc.getLanguage(),
						doc.getImage() ) )
				.observeOn( SwingScheduler.getInstance() )
				.publish();

		// register subscribers
		connectableObservable.subscribe( document -> {
			print( "0 - Updating UI: ", document );
			fetchResult( Arrays.asList( document ) );
		} );
		connectableObservable.subscribe( doc -> print( "1 - ", doc ) );
		connectableObservable.subscribe( doc -> print( "2 - ", doc ) );
		connectableObservable.subscribe( doc -> print( "3 - ", doc ) );

		// connect subject with subscribers
		connectObservable( connectableObservable );
	}

	private rx.Observable<SWPackage<List<Document>, Document>> getRxObservable()
	{
		return rx.Observable.fromEmitter( new SWEmitter<List<Document>, Document>()
		{
			@Override
			protected List<Document> doInBackground() throws Exception
			{
				List<Document> result = new ArrayList<Document>();
				for ( File file : files )
				{
					try
					{
						publish( new Document(
								file.getName(),
								file.getAbsolutePath(),
								Language.values()[ settings.getRecognitionLanguageId() ],
								ImageIO.read( file ) ) );
					}
					catch ( IOException ex )
					{
						log.warn( "Could not read image from file " + file );
					}
				}
				return result;
			}
		}, Emitter.BackpressureMode.BUFFER );
	}

	@Override
	protected void process( List<Document> chunks )
	{
		fetchResult( chunks );
	}

	public abstract void fetchResult( List<Document> result );

	private void print(String id, Document document )
	{
		System.out.println( "[ " + Thread.currentThread().getName() + " ] " + id + document.getName() );
	}
}
