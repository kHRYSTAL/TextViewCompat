fix bug such as:

[Link](https://yangqiuyan.github.io/2018/11/21/LinkMovementMethod/)

![](https://yangqiuyan.github.io/2018/11/21/LinkMovementMethod/pics.png)


- fix 1. if text has span, and need `setMaxLines` && `setEllipsize`, the '...' not show

    watch `onMeasure` and width must set `wrap_content`

- fix 2. if text has span and `setMaxLines` and text in `RecyclerView`, when scroll RecyclerView, and touch on text, often display such as screenshot

    watch `onTouch`

- fix 3. if text has span and need text selectable

    watch `onTouch` and width must set `match_parent`




