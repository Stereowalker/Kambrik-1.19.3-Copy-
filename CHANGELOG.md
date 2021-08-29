# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html) as closely as it can.

## [1.1.0] for 1.17.1 - Unreleased

### Added
- New Keybind API (thanks to isXander and Ejektaflex)
    - Allows devs to easily define and register keybinds
    - Allows possible realtime key updates rather than per tick
    - Allows specifying different pressed and unpressed functionality
- File API
    - getBaseFolder now returns a Folder (File) instead of a Path
    - getBaseFolderPath now returns a Path
- Serial API
    - Add an Identifier serializer
- New Persistence API (Unstable/Experimental)
    - Allows devs to easily add and access persistent data
    - Useful for config data and serverside data
    
  
## [1.0.0] for 1.17.1 - 2021-08-25
- Initial release of Kambrik
