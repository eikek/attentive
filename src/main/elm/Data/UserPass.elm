module Data.UserPass exposing (..)

import Json.Decode as Decode exposing (Decoder, int, string, float, bool)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)
import Json.Encode as Encode

type alias UserPass =
    { account: String
    , password: String
    }

empty: UserPass
empty =
    { account = ""
    , password = ""
    }

encode: UserPass -> Encode.Value
encode up =
    Encode.object
        [ ("account", Encode.string up.account)
        , ("password", Encode.string up.password)
        ]

decoder: Decoder UserPass
decoder =
    Decode.succeed UserPass
        |> required "account" string
        |> required "password" string
