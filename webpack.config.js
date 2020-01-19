const path = require('path');
const fs = require('fs');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const {CleanWebpackPlugin} = require('clean-webpack-plugin');
const ScriptExtHtmlWebpackPlugin = require('script-ext-html-webpack-plugin');
const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const TerserJSPlugin = require('terser-webpack-plugin');
const HashedModuleIdsPlugin = require('webpack/lib/HashedModuleIdsPlugin');
const merge = require("webpack-merge");

const commonConfig = {
    entry: './src/ts/index.ts',
    devtool: 'source-map',
    plugins: [
        new CleanWebpackPlugin(),
        new HtmlWebpackPlugin({
            title: 'Clock Resonator',
            template: "src/ejs/index.ejs"
        }),
        new ScriptExtHtmlWebpackPlugin({
            defaultAttribute: 'defer'
        }),
        new ForkTsCheckerWebpackPlugin({
            silent: true
        }),
    ],
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        cacheDirectory: true,
                    },
                },
                include: path.resolve(__dirname, 'src'),
            },
        ],
    },
    resolve: {
        extensions: ['.tsx', '.ts', '.js'],
    },
    output: {
        path: path.resolve(__dirname, 'dist'),
    },
    optimization: {
        moduleIds: "hashed",
        runtimeChunk: "single",
        minimizer: [
            new TerserJSPlugin(),
            new OptimizeCSSAssetsPlugin(),
        ],
    },
};

// A list of modules that are larger than 244kb,
// just on their own, and trigger asset warnings.
const modulesThatAreJustTooBig = [
    "@firebase/firestore"
].map(name => `npm.${name.replace("/", "~")}`);

module.exports = (env, argv) => {
    if (typeof argv !== 'undefined' && argv['mode'] === 'production') {
        return merge(commonConfig, {
            plugins: [
                new MiniCssExtractPlugin({
                    filename: '[name].css',
                    chunkFilename: '[id].css',
                    ignoreOrder: false,
                }),
                new HashedModuleIdsPlugin({
                    hashFunction: 'sha256',
                    hashDigest: 'hex',
                    hashDigestLength: 20
                }),
            ],
            mode: 'production',
            output: {
                filename: '[name].[contenthash].js'
            },
            module: {
                rules: [
                    {
                        test: /\.css$/i,
                        use: [
                            {
                                loader: MiniCssExtractPlugin.loader,
                            },
                            'css-loader',
                        ],
                    },
                ],
            },
            performance: {
                assetFilter: function(filename) {
                    if (modulesThatAreJustTooBig.some(name => filename.startsWith(name))) {
                        return false;
                    }
                    return !(/\.(map|LICENSE)$/.test(filename));
                },
            },
            optimization: {
                splitChunks: {
                    chunks: 'all',
                    maxAsyncRequests: Infinity,
                    maxInitialRequests: Infinity,
                    cacheGroups: {
                        vendor: {
                            test: /[\\/]node_modules[\\/]/,
                            name(module) {
                                const file = module.context;
                                let currentDir = file;
                                while (true) {
                                    while (!fs.existsSync(path.resolve(currentDir, 'package.json'))) {
                                        if (currentDir.indexOf('node_modules') < 0) {
                                            // we went too far
                                            throw new Error(`Failed to find package.json for ${file}`);
                                        }
                                        currentDir = path.dirname(currentDir);
                                    }
                                    const pkgJson = require(path.resolve(currentDir, 'package.json'));
                                    let packageName = pkgJson['name'];
                                    if (!packageName) {
                                        const requested = pkgJson['_requested'];
                                        packageName = requested && requested['name'];
                                    }
                                    if (!packageName) {
                                        currentDir = path.dirname(currentDir);
                                        continue;
                                    }

                                    return `npm.${packageName.replace("/", "~")}`;
                                }
                            },
                        },
                    },
                },
            },
        });
    }
    return merge(commonConfig, {
        mode: 'development',
        devServer: {
            contentBase: './dist',
            hot: true,
            historyApiFallback: true,
        },
        output: {
            publicPath: "/",
            filename: '[name].[hash].js',
        },
        module: {
            rules: [
                {
                    test: /\.css$/i,
                    use: [
                        'style-loader',
                        'css-loader',
                    ],
                },
            ]
        },
        resolve: {
            alias: {
                'react-dom': '@hot-loader/react-dom',
            },
        },
    });
};