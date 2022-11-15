- data driving for rainglow modes
    - modes are loaded from json through a server datapack
    - when joining a server that has modes unknown to the client, they will be sent over
    - modes define a text colour, an id, and a list of colours

- config sync
    - config sync can be toggled on and off through config
    - when enabled, the client will send its config to the server on join
    - the client cannot edit their config while in a server, either through the file or the screen
    - this mode can be default, custom-defined through json, or even the builtin "custom" mode
    - the client's config is reset when leaving the server

- improvements to config parser
- stability improvements to data tracker registration