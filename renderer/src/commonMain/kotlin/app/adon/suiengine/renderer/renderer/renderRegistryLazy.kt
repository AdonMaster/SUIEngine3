package app.adon.suiengine.renderer.renderer

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.adon.suiengine.ast.Node
import app.adon.suiengine.ast.toNode
import app.adon.suiengine.renderer.contexts.Context
import app.adon.suiengine.renderer.extensions.registerComponent
import app.adon.suiengine.renderer.layout.LayoutScope
import app.adon.suiengine.renderer.renderer.props.LazyColProps
import app.adon.suiengine.renderer.renderer.props.LazyRowProps
import app.adon.suiengine.renderer.utils.ter
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
import com.composables.icons.lucide.ImageOff
import com.composables.icons.lucide.Lucide
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
val renderRegistryLazy = buildMap<String, @Composable (Node.Fn, Context) -> Unit> {

    registerComponent(
        "lazy_col",
        resolveProps = { node, context -> LazyColProps.from(node, context) }
    ) { props, context ->

        val state = rememberLazyListState()
        val arr = props.array.v
        LazyColumn(
            state = state,
            modifier = props.modifier,
            verticalArrangement = props.vArrange,
            horizontalAlignment = props.hAlign,
        ) {
            items(
                count = arr.size,
                key = { i -> "${i}_${arr[i].stringableVal()}" }
            ) { index ->
                val childContext = context.newChild("lazy_col", LayoutScope.None)
                childContext.setVirtual(props.asName, arr[index])
                childContext.setVirtual(props.indexName, index.toNode())

                RenderGroup(props.children, childContext)
            }
        }

    }

    registerComponent(
        "lazy_row",
        resolveProps = { node, context -> LazyRowProps.from(node, context) }
    ) { props, context ->

        val state = rememberLazyListState()
        var snap = if (props.snap) {
            rememberSnapFlingBehavior(lazyListState = state)
        } else {
            ScrollableDefaults.flingBehavior()
        }

        val arr = props.array.v
        LazyRow(
            state = state,
            flingBehavior = snap,
            modifier = props.modifier,
            horizontalArrangement = props.hArrange,
            verticalAlignment = props.vAlign,
        ) {
            items(
                count = arr.size,
                key = { i -> "${i}_${arr[i].stringableVal()}" }
            ) { index ->
                val childContext = context.newChild("lazy_row", LayoutScope.None)
                childContext.setVirtual(props.asName, arr[index])
                childContext.setVirtual(props.indexName, index.toNode())

                RenderGroup(props.children, childContext)
            }
        }
    }

}
