module Data.LoginResult exposing (..)

import Json.Decode as Decode exposing (Decoder, int, string, float, bool)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)
import Json.Encode as Encode

type alias LoginResult =
    { account: String
    , success: Bool
    , message: String
    , authenticator: Maybe String
    }

empty: LoginResult
empty =
    { account = ""
    , success = False
    , message = ""
    , authenticator = Nothing
    }

decoder: Decoder LoginResult
decoder =
    Decode.succeed LoginResult
        |> required "account" string
        |> required "success" bool
        |> required "message" string
        |> required "authenticator" (Decode.maybe string)
