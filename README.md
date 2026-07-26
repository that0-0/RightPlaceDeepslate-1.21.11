# Block Rotation Lock

## Features

This mod lets you lock the placement axis for blocks with the axis property (Logs, Pillars, Deepslate, Basalt, etc.).

Press the lock key (default: H) to set the lock direction. (Targeted block face or view direction if no block is targeted)

While the lock is active, all axis-based blocks you place will be oriented along the locked axis.

This is achieved by modifying the `BlockHitResult` of your placement action. A server running cheat detection might notice that you're trying to place a block in a way you shouldn't be able to. Use this mod only on servers where you know it's allowed.

I only made this mod because I was using a lot of raw Deepslate for a build and didn't like the fact that it can be placed like logs.


## License

This mod is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.
