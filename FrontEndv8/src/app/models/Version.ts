export class Version {
  major : number;
  minor : number;
  build : number;

  public static asString(version: Version) {
    if (!version)
      return '';

    let result : string = version.major.toString();
    if (version.minor)
      result += '.' + version.minor.toString();
    else
      result += '.0';

    if (version.build)
      result += '.' + version.build.toString();
    else
      result += '.0';

    return result;
  }

  public static compare(first: Version, second: Version) : number {
    if (!first) return null;
    if (!second) return null;
    if (first.major - second.major != 0)
      return first.major - second.major;

    if (first.minor - second.minor != 0)
      return first.minor - second.minor;

    if (first.build - second.build != 0)
      return first.build - second.build;

    return 0;
  }
}
