port module Ports exposing (..)

import Data.Account exposing (Account)

port initElements: () -> Cmd msg

port setAccount: (Account, String) -> Cmd msg
port removeAccount: String -> Cmd msg

--port reloadPage: () -> Cmd msg
