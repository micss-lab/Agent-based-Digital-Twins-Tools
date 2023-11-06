import configparser
import os
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from uwbmap.mainwindow.map.robot import Robot


def read_config(config_file: str) -> configparser.ConfigParser:
    config = configparser.ConfigParser()
    config.read(config_file)
    if not config.sections():
        config = configparser.ConfigParser()
        config["MQTT"] = {"host": "localhost", "port": "1883", "topic": "tags", "websockets": "False"}
        config["Tags"] = {}
        with open(config_file, "w") as configfile:
            config.write(configfile)
    return config


def save_config(config_file: str, config: configparser.ConfigParser, tags: dict[str, "Robot"]):
    for tag_id, tag in tags.items():
        if tag.dual_agent:
            continue

        config["Tag:" + tag_id] = {}
        config["Tag:" + tag_id]["address"] = tag.address
        config["Tag:" + tag_id]["guid"] = tag.guid
        config["Tag:" + tag_id]["colour"] = tag.graphics.colour.name()

    with open(config_file, "w") as configfile:
        config.write(configfile)
