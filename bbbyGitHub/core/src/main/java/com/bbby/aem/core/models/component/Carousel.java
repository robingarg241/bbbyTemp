package com.bbby.aem.core.models.component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.inject.Inject;

/**
 * Carousel Component compatible with <a href="https://owlcarousel2.github.io/OwlCarousel2/">Owl</a> carousel framework.
 */
@Model(adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL,
    resourceType = "bbby/components/content/carousel")
@Exporter(name = "jackson", extensions = "json")
public class Carousel extends ComponentSlingModel {

    private static final Gson GSON = new GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .create();

    /*
     * The below fields are the owl configuration variables.
     *
     * The @Exposed fields are serialized to JSON and stored in the data-owl attribute that is used for initialization.
     */

    @Expose
    @SerializedName("items")
    private int slidesToShow;

    @Expose
    @SerializedName("loop")
    private boolean loop;

    @Expose
    @SerializedName("slideBy")
    private int slidesToScroll;

    @Expose
    @SerializedName("navText")
    private String[] navigationText;

    @Inject
    @ValueMapValue
    @Expose
    @SerializedName("dots")
    private boolean dots;

    @Inject
    @ValueMapValue
    private boolean moveDotsUp;

    @Inject
    @ValueMapValue
    @Expose
    @SerializedName("nav")
    private boolean arrows;

    @Inject
    @ValueMapValue
    @Expose
    @SerializedName("autoHeight")
    private boolean adaptiveHeight;

    @Inject
    @ValueMapValue
    @Expose
    @SerializedName("autoplay")
    private boolean autoplay;

    @Inject
    @ValueMapValue
    @Default(intValues = 3000)
    @Expose
    @SerializedName("autoplayTimeout")
    private int autoplaySpeed;

    @Inject
    @ValueMapValue
    @Default(intValues = 300)
    @Expose
    @SerializedName("smartSpeed")
    private int speed;

    @Override
    public void postConstruct() throws Exception {
        super.postConstruct();

        slidesToScroll = 1;
        slidesToShow = 1;
        loop = true;

        // Force carousel to not print out any special markup for arrows. CSS will handle this.
        navigationText = new String[] { " ", " " };
    }

    public String getCarouselConfigAttribute() {
        return GSON.toJson(this);
    }

    public boolean isMoveDotsUp() {
        return moveDotsUp;
    }

    public boolean isDots() {
        return dots;
    }

    public boolean isArrows() {
        return arrows;
    }

    public int getSlidesToShow() {
        return slidesToShow;
    }

    public boolean isLoop() {
        return loop;
    }

    public int getSlidesToScroll() {
        return slidesToScroll;
    }

    public String[] getNavigationText() {
        if (navigationText != null) return navigationText.clone();
        return new String[0];
    }

}
